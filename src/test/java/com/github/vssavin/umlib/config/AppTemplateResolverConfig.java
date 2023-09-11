package com.github.vssavin.umlib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * @author vssavin on 17.05.2022.
 */

//Example of adding some template resolvers
@Configuration
public class AppTemplateResolverConfig {

    //Add this code before starting your Spring application or set the property from the jvm args
    //This code must be executed before creating UmTemplateResolverConfig
    /*
    static {
        System.setProperty("um.templateResolver.order", "1");
    }
    */

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver umTemplateResolver) {
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(umTemplateResolver);
        return springTemplateEngine;
    }

    @Bean
    public SpringResourceTemplateResolver appTemplateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/template/test/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(0);
        templateResolver.setCheckExistence(true);
        return templateResolver;
    }

    @Bean
    public ThymeleafViewResolver appViewResolver(SpringTemplateEngine templateEngine) {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        templateEngine.addTemplateResolver(appTemplateResolver());
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setOrder(0);
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }
}
