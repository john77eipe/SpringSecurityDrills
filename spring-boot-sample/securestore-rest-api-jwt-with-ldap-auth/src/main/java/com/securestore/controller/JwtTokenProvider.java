package com.securestore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Collection;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;
    
    


    public String generateToken(String username, Authentication authentication) {

    	LdapUserDetailsImpl userPrincipal = (LdapUserDetailsImpl) authentication.getPrincipal();
    	Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
    	
    	 String organizationalUnit = "";
    	 String givenName = "";

         
         // null check on authentication omitted
         Object principal = authentication.getPrincipal();
         if(principal instanceof InetOrgPerson)
         {
         	organizationalUnit = ((InetOrgPerson)principal).getOu();
         	givenName = ((InetOrgPerson) principal).getGivenName();
         	System.out.println(organizationalUnit);
         }
         
     
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return createJwtToken(username, givenName, organizationalUnit);
    }

    public String getUsernameFromJWT(String jwtToken) {
//        Claims claims = Jwts.parser()
//                .setSigningKey(jwtSecret)
//                .parseClaimsJws(token)
//                .getBody();
//
//        return claims.getSubject();
    	DecodedJWT jwt = JWT.decode(jwtToken);
    	Claim usernameClaim = jwt.getClaim(CommonConstants.TOKENDAO_USER_NAME);
    	Claim roleClain = jwt.getClaim(CommonConstants.TOKENDAO_ROLE);
    	return usernameClaim.asString();
    }

    public boolean validateToken(String jwtToken) {
    	Algorithm algorithm = Algorithm.HMAC256(CommonConstants.SECRET_KEY);
		JWTVerifier verifier = JWT.require(algorithm).build();
		verifier.verify(jwtToken);

    	DecodedJWT jwt = JWT.decode(jwtToken);
    	Claim usernameClaim = jwt.getClaim(CommonConstants.TOKENDAO_USER_NAME);
    	Claim roleClain = jwt.getClaim(CommonConstants.TOKENDAO_ROLE);
    	return true;
//        try {
//            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
//            return true;
//        } catch (SignatureException ex) {
//            logger.error("Invalid JWT signature");
//        } catch (MalformedJwtException ex) {
//            logger.error("Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            logger.error("Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            logger.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            logger.error("JWT claims string is empty.");
//        }

    }
    
    public String createJwtToken(String username, String givenName, String role) {
		String secretKey = CommonConstants.SECRET_KEY;
		Algorithm algorithm = Algorithm.HMAC256(secretKey);
		Date date = new Date();
		long t = date.getTime();
		Date expirationTime = new Date(t + CommonConstants.EXPIRATION_UPDATE_TIME);
		String jwtToken = JWT.create().withClaim(CommonConstants.TOKENDAO_USER_NAME, username)
				.withClaim(CommonConstants.TOKENDAO_TOKEN, username).withClaim(CommonConstants.TOKENDAO_ROLE, role)
				.withClaim(CommonConstants.TOKENDAO_GIVEN_NAME, givenName)
				.sign(algorithm);
//		TokenDao tokenObj = new TokenDao();
//		tokenObj.setToken(token);
//		tokenObj.setStatus(CommonConstants.TOKENDAO_LOGGED_IN);
//		tokenObj.setUsername(username);
//		tokenObj.setExpiry(expirationTime);
//		saveToken(tokenObj);
		return jwtToken;
	}
}