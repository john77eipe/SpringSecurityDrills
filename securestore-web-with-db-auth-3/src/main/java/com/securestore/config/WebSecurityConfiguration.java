package com.securestore.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * @author John Eipe
 * @EnableGlobalMethodSecurity is needed for method level security specification
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired private AuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired private AuthenticationFailureHandler authenticationFailureHandler;
    
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserDetailsService userDetailsService;
    
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
       
    	
		//Note that the order of the antMatchers() elements is significant 
		//the more specific rules need to come first, followed by the more general ones
		httpSecurity
		.csrf().disable()
		.authorizeRequests()
	        .antMatchers("/user/**").hasAnyRole("USER", "HRADMIN")
	        .antMatchers("/admin").hasRole("HRADMIN")
	        .antMatchers("/login*").permitAll()
	        .antMatchers("/**", "/css/**", "/js/**", "/images/**").permitAll()
	        .anyRequest().authenticated()
        .and()
        .formLogin()
	        .loginPage("/")
	        .loginProcessingUrl("/login")
        .failureHandler(authenticationFailureHandler)//.failureUrl("/?login_error") //using handler for debugging
        .successHandler(authenticationSuccessHandler)
        .and()
	        .logout()
	        .logoutUrl("/logout")
        .deleteCookies("JSESSIONID");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	
    	auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    	
    }
    @Bean
    PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }
    
}
