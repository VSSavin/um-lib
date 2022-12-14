package io.github.vssavin.umlib;

import io.github.vssavin.umlib.utils.UmUtil;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by vssavin on 16.05.2022.
 */
@SpringBootApplication
public class Launcher {

    static {
        System.setProperty("um.templateResolver.order", "1");
    }

    public static void main(String[] args) {
        DOMConfigurator.configure("./log4j.xml");
        //UmUtil.setApplicationArguments(args);     //now used only if spring boot not used
        SpringApplication.run(Launcher.class, args);
    }
}
