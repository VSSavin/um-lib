package io.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * @author vssavin on 15.05.2022
 */
@Configuration
public class UmTemplateResolverConfig {
    private final Logger log = LoggerFactory.getLogger(UmTemplateResolverConfig.class);

    @Bean
    public SpringResourceTemplateResolver umTemplateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/um/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        String orderString = System.getProperty("um.templateResolver.order");
        int order = 0;
        if (orderString != null) {
            try {
                order = Integer.parseInt(orderString);
            } catch (NumberFormatException nfe) {
                log.error("Template resolver order should be integer value!");
            }
        }
        templateResolver.setOrder(order);
        templateResolver.setCheckExistence(true);

        return templateResolver;
    }
}
