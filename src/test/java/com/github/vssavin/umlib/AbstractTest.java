package com.github.vssavin.umlib;

import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.config.UmTemplateResolverConfig;
import com.github.vssavin.umlib.domain.security.SecureService;
import com.github.vssavin.umlib.config.ApplicationConfig;
import com.github.vssavin.umlib.domain.user.User;
import com.github.vssavin.umlib.domain.user.UserDatabaseInitService;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
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
@ActiveProfiles("um-test")
@SpringBootTest(args = {"authService=rsa"}, properties = "spring.main.allow-bean-definition-overriding=true")
@ContextConfiguration(classes = {ApplicationConfig.class, UmTemplateResolverConfig.class})
@WebAppConfiguration
public abstract class AbstractTest {
    private static final String DEFAULT_SECURE_ENDPOINT = "/um/security/key";

    static {
        DOMConfigurator.configure("./log4j.xml");
    }

    protected MockMvc mockMvc;
    protected SecureService secureService;
    protected UserDatabaseInitService userDatabaseInitService;
    private WebApplicationContext context;

    @Autowired
    public void setContext(WebApplicationContext context) {
        this.context = context;
    }

    @Autowired
    public void setUmConfig(UmConfig umConfig) {
        secureService = umConfig.getSecureService();
    }

    @Autowired
    public void setUserDatabaseInitService(UserDatabaseInitService dataBaseInitServiceUser) {
        this.userDatabaseInitService = dataBaseInitServiceUser;
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
