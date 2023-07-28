package com.github.vssavin.umlib;

import com.github.vssavin.umlib.config.*;
import com.github.vssavin.umlib.security.SecureService;
import com.github.vssavin.umlib.user.User;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * @author vssavin on 07.01.2022
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("um-test")
@SpringBootTest(args = {"authService=rsa"}, properties = "spring.main.allow-bean-definition-overriding=true")
@ContextConfiguration(classes = {ApplicationConfig.class, UmTemplateResolverConfig.class})
@WebAppConfiguration
public abstract class AbstractTest {
    private static final String DEFAULT_SECURE_ENDPOINT = "/um/security/key";

    @Rule
    public TestName testName = new TestName();

    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);

    private SettableUmRoutingDatasource settableUmRoutingDatasource;

    static {
        DOMConfigurator.configure("./log4j_tests.xml");
    }

    protected MockMvc mockMvc;
    protected SecureService secureService;
    private WebApplicationContext context;

    @Autowired
    public void setContext(WebApplicationContext context) {
        this.context = context;
    }

    @Autowired
    public void setUmConfig(UmConfig umConfig) {
        secureService = umConfig.getAuthService();
    }

    @Autowired
    public void setRoutingDatasourceWrapper(SettableUmRoutingDatasource routingDataSource) {
        this.settableUmRoutingDatasource = routingDataSource;
    }

    @Before
    public void initScripts() {
        String threadName = Thread.currentThread().getName();
        String simpleClassName = this.getClass().getSimpleName();
        log.debug("[{}]-[{}]: Test method: [{}.{}]", threadName, simpleClassName,
                simpleClassName, testName.getMethodName());
        log.debug("[{}]-[{}]: Started initScripts method...", threadName, simpleClassName);
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(H2)
                .setScriptEncoding("UTF-8")
                .addScript("/init_test.sql")
                .ignoreFailedDrops(true)
                .build();
        log.debug("[{}]-[{}]: Using datasource: {}", threadName, this.getClass().getSimpleName(), dataSource);
        settableUmRoutingDatasource.setUmDataSource(dataSource);
    }

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }))
                .build();
    }

    protected RequestPostProcessor getRequestPostProcessorForUser(User user) {
        return user(user.getLogin()).password(user.getPassword()).roles(user.getAuthority());
    }

    protected String encrypt(String secureEndpoint, String data) throws Exception {
        String urlTemplate = secureEndpoint;
        if (urlTemplate == null || urlTemplate.isEmpty()) {
            urlTemplate = DEFAULT_SECURE_ENDPOINT;
        }
        ResultActions secureAction = mockMvc.perform(get(urlTemplate));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        return secureService.encrypt(data, secureKey);
    }

    protected String decrypt(String secureEndpoint, String data) throws Exception {
        String urlTemplate = secureEndpoint;
        if (urlTemplate == null || urlTemplate.isEmpty()) {
            urlTemplate = DEFAULT_SECURE_ENDPOINT;
        }
        ResultActions secureAction = mockMvc.perform(get(urlTemplate));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        return secureService.decrypt(data, secureKey);
    }
}
