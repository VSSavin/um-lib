package com.github.vssavin.umlib;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by vssavin on 16.05.2022.
 */
@SpringBootApplication
public final class Launcher {

    static {
        System.setProperty("um.templateResolver.order", "1");
    }

    private Launcher() {

    }

    public static void main(String[] args) {
        DOMConfigurator.configure("./log4j.xml");
        SpringApplication.run(Launcher.class, args);
    }
}
