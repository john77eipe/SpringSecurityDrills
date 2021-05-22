package com.securestore.config;

import com.securestore.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @author John Eipe
 *
 */
@Configuration
@EnableWebSecurity
@Order(1)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired private AuthenticationSuccessHandler authenticationSuccessHandler;
	@Autowired private AuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    @Qualifier("userAccountDetailsService")
    UserAccountDetailsService userAccountDetailsService;
    
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

		// @formatter:off
		//Note that the order of the antMatchers() elements is significant 
		//the more specific rules need to come first, followed by the more general ones
		/*httpSecurity
		.csrf().disable()
		.authorizeRequests()
			.antMatchers("/rest/user/**").hasAnyRole("USER", "HRADMIN", "SUPERADMIN")
			.antMatchers("/userPage/*").hasAnyRole("USER", "HRADMIN")
	        .antMatchers("/adminPage/*").hasRole("HRADMIN")
	        .antMatchers("/login*").permitAll()
	        .antMatchers("/**", "/css/**", "/js/**", "/images/**").permitAll()
			.antMatchers("/oauth/token").permitAll()
	        .and()
	        .formLogin()
		        .loginPage("/")
		        .loginProcessingUrl("/login")
	        .failureHandler(authenticationFailureHandler)
	        .successHandler(authenticationSuccessHandler)
	        .and()
		        .logout()
		        .logoutUrl("/logout")
	        .deleteCookies("JSESSIONID");*/
		httpSecurity
            .csrf().disable()
//            .anonymous().disable()

//        .requestMatchers()
//                .antMatchers(HttpMethod.POST, "/login")
//                .antMatchers(HttpMethod.POST, "/logout")
//                .antMatchers("/oauth/authorize")
//                .and()

			.authorizeRequests()
                .antMatchers("/**", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/oauth/token").permitAll()
            .antMatchers(HttpMethod.GET,"/userPage/*").hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.GET, "/adminPage/*").hasRole("ADMIN")

                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/loginPage")
                .loginProcessingUrl("/login")
                .failureHandler(authenticationFailureHandler)
                .successHandler(authenticationSuccessHandler)
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout**"))
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID");


		// @formatter:on
    }

    @Autowired
    protected void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
    	auth.userDetailsService(userAccountDetailsService).passwordEncoder(passwordEncoder);
		/*auth.inMemoryAuthentication()
				.withUser("bill").password("{noop}abc123").roles("ADMIN").and()
				.withUser("bob").password("{noop}abc123").roles("USER");*/
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
