package com.securestore.config;

import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static com.securestore.config.TokenConstants.*;

public class TokenBasedAuthorizationFilter extends BasicAuthenticationFilter {
    TokenBasedAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authorizationToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationToken != null && authorizationToken.startsWith(TOKEN_PREFIX)) {
            authorizationToken = authorizationToken.replaceFirst(TOKEN_PREFIX, "");
            String username = Jwts.parser()
                    .setSigningKey(TOKEN_SECRET)
                    .parseClaimsJws(authorizationToken)
                    .getBody()
                    .getSubject();
            SecurityContextHolder.getContext()
                    .setAuthentication(new CustomAuthenticationToken(username, null, domain));
        }
        chain.doFilter(request, response);
    }
}