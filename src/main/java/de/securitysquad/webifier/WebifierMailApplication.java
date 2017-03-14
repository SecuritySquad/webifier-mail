package de.securitysquad.webifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by samuel on 07.03.17.
 */
@SpringBootApplication
@ComponentScan("de.securitysquad.webifier")
//@PropertySource("classpath:application.properties")
public class WebifierMailApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebifierMailApplication.class, args);
        context.registerShutdownHook();
    }
}
