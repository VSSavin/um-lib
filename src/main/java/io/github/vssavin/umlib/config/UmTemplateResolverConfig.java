package io.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * @author vssavin on 15.05.2022
 */
@Configuration
public class UmTemplateResolverConfig implements WebMvcConfigurer {
    private final Logger log = LoggerFactory.getLogger(UmTemplateResolverConfig.class);
    private static final SpringTemplateEngine SPRING_TEMPLATE_ENGINE = new SpringTemplateEngine();

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
    public SpringTemplateEngine templateEngine() {
        SPRING_TEMPLATE_ENGINE.addTemplateResolver(umTemplateResolver());
        return SPRING_TEMPLATE_ENGINE;
    }

    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setOrder(getResolverOrder());
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry
                .addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry
                .addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/flags/**").addResourceLocations("classpath:/static/flags/");

        registry.addResourceHandler("/**").addResourceLocations("classpath:/template/um/");
    }

    public static SpringTemplateEngine getSpringTemplateEngine() {
        return SPRING_TEMPLATE_ENGINE;
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
