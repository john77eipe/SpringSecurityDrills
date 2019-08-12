package com.securestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.web.context.request.RequestContextListener;

/**
 * @author John Eipe
 * @since 29th Apr 2019
 */
@SpringBootApplication
public class ApplicationR1 {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationR1.class, args);
    }
    

    /*@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}*/

	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}
    
}
