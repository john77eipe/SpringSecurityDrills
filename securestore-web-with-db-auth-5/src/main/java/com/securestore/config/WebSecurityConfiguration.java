package com.securestore.config;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.securestore.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
    @Autowired private UserAccountDetailsService userDetailsService;
    
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

//		RememberMeServices rememberMeServices = new CustomTokenBasedRememberMeServices("uniqueAndSecret", userDetailsService);
    	
		//Note that the order of the antMatchers() elements is significant 
		//the more specific rules need to come first, followed by the more general ones
		httpSecurity
		.csrf().disable()
				.addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//				.addFilterBefore(
//						new RememberMeAuthenticationFilter(
//								authenticationManagerBean(),
//								rememberMeServices), RememberMeAuthenticationFilter.class)
		.authorizeRequests()
			.antMatchers("/rest/user/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
			.antMatchers("/userPage/**").hasAnyRole("USER", "ADMIN")
	        .antMatchers("/adminPage/**").hasRole("ADMIN")
	        .antMatchers("/loginPage").permitAll()
			.antMatchers("/register/**").permitAll()
	        .antMatchers("/**", "/css/**", "/js/**", "/images/**").permitAll()
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
	        	.deleteCookies("JSESSIONID")
			.and()
				.rememberMe()
				.rememberMeParameter("rememberMe")
				.rememberMeServices(customTokenBasedRememberMeServices())
			.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			.and()
				.sessionManagement().maximumSessions(1);
    }

	@Bean
	public CustomTokenBasedRememberMeServices customTokenBasedRememberMeServices(){
		CustomTokenBasedRememberMeServices rememberMeServices = new CustomTokenBasedRememberMeServices("uniqueAndSecret", userDetailsService);
		rememberMeServices.setParameter("rememberMe");
		//rememberMeServices.setTokenValiditySeconds(1209600);
		return rememberMeServices;
	}

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	//auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		auth.authenticationProvider(authProvider());
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder(); //or use custom implementation new PasswordEncryption()
    }
    
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


	public AuthenticationProvider authProvider() {
		UserAccountAuthenticationProvider provider
				= new UserAccountAuthenticationProvider(passwordEncoder, userDetailsService);
		return provider;
	}

	public CustomAuthenticationFilter authenticationFilter() throws Exception {
		CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
		filter.setAuthenticationManager(authenticationManagerBean());
		//you could define handlers here too
		//filter.setAuthenticationFailureHandler(authenticationFailureHandler);
		//filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
		return filter;
	}
    
}
