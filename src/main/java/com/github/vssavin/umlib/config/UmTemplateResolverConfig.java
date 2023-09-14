package com.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Configuration of user management template resolvers.
 *
 * @author vssavin on 15.05.2022
 */
@Configuration
public class UmTemplateResolverConfig implements WebMvcConfigurer {
    private final Logger log = LoggerFactory.getLogger(UmTemplateResolverConfig.class);

    private final UmConfigurer umConfigurer;

    public UmTemplateResolverConfig(UmConfigurer umConfigurer) {
        this.umConfigurer = umConfigurer;
    }

    @Bean
    public SpringResourceTemplateResolver umTemplateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/template/um/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        int order = getResolverOrder();
        templateResolver.setOrder(order);
        templateResolver.setCheckExistence(true);

        return templateResolver;
    }

    @Bean
    public ThymeleafViewResolver viewResolver(SpringTemplateEngine templateEngine) {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setOrder(getResolverOrder());
        viewResolver.setCharacterEncoding("UTF-8");
        templateEngine.addTemplateResolver(umTemplateResolver());
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        umConfigurer.getResourceHandlers().forEach((handler, locations) ->
                registry.addResourceHandler(handler).addResourceLocations(locations));
    }

    private int getResolverOrder() {
        String orderString = System.getProperty("um.templateResolver.order");
        int order = 0;
        if (orderString != null) {
            try {
                order = Integer.parseInt(orderString);
            } catch (NumberFormatException nfe) {
                log.error("Template resolver order should be integer value!");
            }
        }

        return order;
    }
}
