package com.securestore.config;

import java.util.Date;

import com.securestore.domain.CustomSecurityUser;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${securestore.app.jwtSecret}")
	private String jwtSecret;

	@Value("${securestore.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	//creates a spec-compliant secure-random key:
	private static SecretKey hmacKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	//JwtParser is immutable, it is thread-safe
	private static JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(hmacKey).build();


	public String generateJwtToken(Authentication authentication) {

		CustomSecurityUser userPrincipal = (CustomSecurityUser) authentication.getPrincipal();
		System.out.println(userPrincipal.getAuthorities().stream().toArray());
		return Jwts.builder()
				.setSubject((userPrincipal.getUsername()))
				.claim("role", userPrincipal.getAuthorities().stream().toArray())
				.setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(hmacKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return jwtParser.parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			jwtParser.parseClaimsJws(authToken);
			return true;
		} catch (JwtException  e) {
			logger.error("JWT issue: {}", e.getMessage());
		}

		return false;
	}
}