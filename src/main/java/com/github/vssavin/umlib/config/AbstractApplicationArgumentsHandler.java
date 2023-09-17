package com.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The base abstract class for handling application arguments.
 *
 * @author vssavin on 16.09.2023
 */
abstract class AbstractApplicationArgumentsHandler {

    private final Logger log;

    private final BeanFactory beanFactory;

    protected AbstractApplicationArgumentsHandler(Logger log, BeanFactory beanFactory) {
        this.log = log;
        this.beanFactory = beanFactory;
    }

    protected AbstractApplicationArgumentsHandler(BeanFactory beanFactory) {
        log = LoggerFactory.getLogger(AbstractApplicationArgumentsHandler.class);
        this.beanFactory = beanFactory;
    }

    protected abstract void processArgs();

    protected String[] getApplicationArguments() {
        try {
            Object appArgsBean = beanFactory.getBean("springApplicationArguments");
            Method sourceArgsMethod = appArgsBean.getClass().getMethod("getSourceArgs");
            String[] args = (String[]) sourceArgsMethod.invoke(appArgsBean);
            if (args != null && args.length > 0) {
                return args;
            }
        }
        catch (NoSuchBeanDefinitionException ignore) { // ignore
        }
        catch (NoSuchMethodException e) {
            log.error("Method \"getSourceArgs\" not found!", e);
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Method invocation error", e);
        }
        return new String[] {};
    }

    protected static Map<String, String> getMappedArgs(String[] args) {
        Map<String, String> resultMap = new HashMap<>();
        if (args.length > 0) {
            for (String arg : args) {
                String[] params = arg.replace("--", "").split("=");
                if (params.length > 0) {
                    String value = params.length > 1 ? params[1] : "";
                    resultMap.put(params[0], value);
                }
            }
        }
        return resultMap;
    }

}
