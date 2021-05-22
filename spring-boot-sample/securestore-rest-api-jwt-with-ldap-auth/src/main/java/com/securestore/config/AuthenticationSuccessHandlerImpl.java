package com.securestore.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author John Eipe
 * 
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_HRADMIN");
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        String organizationalUnit = "";
            
            // null check on authentication omitted
            Object principal = authentication.getPrincipal();
            if(principal instanceof InetOrgPerson)
            {
            	organizationalUnit = ((InetOrgPerson)principal).getOu();
            	System.out.println(organizationalUnit);
            }
            
        if (organizationalUnit.contains("hradmin") ){
            redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, "/admin");
        } else {
            redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, "/user");
        }
        
    }
}
