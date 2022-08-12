package io.github.vssavin.umlib.tests;

import io.github.vssavin.umlib.config.ApplicationConfig;
import io.github.vssavin.umlib.config.UmTemplateResolverConfig;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.utils.UmUtil;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * @author vssavin on 07.01.2022
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(args = {"authService=rsa"})
@ContextConfiguration(classes = {ApplicationConfig.class, UmTemplateResolverConfig.class})
@WebAppConfiguration
@Sql(scripts = "classpath:init_test.sql", config = @SqlConfig(encoding = "UTF-8"))
public abstract class AbstractTest {
    private static final String DEFAULT_SECURE_ENDPOINT = "/secure/key";

    static {
        DOMConfigurator.configure("./log4j.xml");
    }

    protected MockMvc mockMvc;
    protected SecureService secureService;
    private WebApplicationContext context;

    @Autowired
    public void setContext(WebApplicationContext context) {
        this.context = context;
    }

    @Autowired
    public void setApplicationUtil(UmUtil umUtil) {
        secureService = umUtil.getAuthService();
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
