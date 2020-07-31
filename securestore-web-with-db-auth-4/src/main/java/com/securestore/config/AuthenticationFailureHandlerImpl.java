package com.securestore.config;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component("authenticationFailureHandler")
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

	Logger logger = LogManager.getLogger(AuthenticationFailureHandlerImpl.class);

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException authenticationException) throws IOException, ServletException {
		logger.debug("Authentication failed for user " + authenticationException.getMessage());
		redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, "/generic-error");
	}
}
