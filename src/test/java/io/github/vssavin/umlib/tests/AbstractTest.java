package io.github.vssavin.umlib.tests;

import io.github.vssavin.umlib.config.ApplicationConfig;
import io.github.vssavin.umlib.config.UmTemplateResolverConfig;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.utils.UmUtil;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * @author vssavin on 07.01.2022
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(args = {"authService=rsa"})
@ActiveProfiles("dev")
@ContextConfiguration(classes = {ApplicationConfig.class, UmTemplateResolverConfig.class})
@WebAppConfiguration
@Sql(scripts = "classpath:init_test.sql", config = @SqlConfig(encoding = "UTF-8"))
public abstract class AbstractTest {

    protected MockMvc mockMvc;
    protected SecureService secureService;
    private WebApplicationContext context;
    private Filter springSecurityFilterChain;

    @Autowired
    public void setContext(WebApplicationContext context) {
        this.context = context;
    }

    @Autowired
    public void setSpringSecurityFilterChain(Filter springSecurityFilterChain) {
        this.springSecurityFilterChain = springSecurityFilterChain;
    }

    @Autowired
    public void setApplicationUtil(UmUtil umUtil) {
        secureService = umUtil.getAuthService();
    }

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }))
                .addFilter(springSecurityFilterChain)
                .build();
    }

    protected RequestPostProcessor getRequestPostProcessorForUser(User user) {
        return user(user.getLogin()).password(user.getPassword()).roles(user.getAuthority());
    }
}
