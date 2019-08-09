package com.securestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author John Eipe
 * @since 29th Apr 2019
 */
@SpringBootApplication
public class ApplicationOAuth1 extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationOAuth1.class, args);
    }
}
