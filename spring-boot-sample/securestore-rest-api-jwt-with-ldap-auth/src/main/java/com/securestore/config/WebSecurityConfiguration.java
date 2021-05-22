package com.securestore.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * @author John Eipe
 *
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired private AuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Value("${ldap.urls}")
    private String ldapUrls;
    
    @Value("${ldap.base.dn}")
    private String ldapBaseDn;
    
    @Value("${ldap.username}")
    private String ldapSecurityPrincipal;
    
    @Value("${ldap.password}")
    private String ldapPrincipalPassword;
    
    @Value("${ldap.user.dn.pattern}")
    private String ldapUserDnPattern;
    
    @Value("${ldap.enabled}")
    private String ldapEnabled;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationFailureHandler() {
			
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				exception.printStackTrace();
				
			}
		};
		
		httpSecurity
			.csrf()
			.disable()
		.cors()
			.disable()
		.exceptionHandling()
			.authenticationEntryPoint(new AuthenticationEntryPoint() {
				
				@Override
				public void commence(HttpServletRequest request, HttpServletResponse response,
						AuthenticationException authException) throws IOException, ServletException {
					authException.printStackTrace();
					
				}
			})
		.and()
		.authorizeRequests().antMatchers("/api/auth/**").permitAll()
		.antMatchers("/api/index/**").hasRole("hradmin")
		.anyRequest().authenticated()
		.and()
		.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
		.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		/**
		httpSecurity.authorizeRequests().antMatchers("/**", "/css/**", "/js/**", "/images/**").permitAll()
//                .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
//                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/")
                .loginProcessingUrl("/login")
                .failureHandler(authenticationFailureHandler)//.failureUrl("/?login_error") //using handler for debugging
                .successHandler(authenticationSuccessHandler);
                **/
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	
    	if(Boolean.parseBoolean(ldapEnabled)) {
    		 auth.ldapAuthentication().userSearchFilter("(uid={0})")
	    		 .contextSource()
		    		 .url(ldapUrls + ldapBaseDn)
		    		 .managerDn(ldapSecurityPrincipal)
		    		 .managerPassword(ldapPrincipalPassword)
		    	 .and()
		    	 .userDetailsContextMapper(
		                 new InetOrgPersonContextMapper())
	    		 //.userDnPatterns(ldapUserDnPattern)
		    	 .passwordCompare().passwordAttribute("userPassword")
		            .and()
		            .passwordEncoder(passwordEncoder());
    	} else {
    		System.err.println("No other auth mechanisms configured!!!");
    	}
    }
    
    private PasswordEncoder passwordEncoder() {
    	final PasswordEncoder sha = new LdapShaPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return sha.encode(rawPassword);
           
            }            
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return sha.matches(rawPassword.toString(), encodedPassword);
            }
        };
    }
    
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
}
