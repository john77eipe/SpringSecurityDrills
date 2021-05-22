package com.securestore.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    
    @Autowired
    JwtTokenProvider tokenProvider;
    
    
    String INVALID_TOKEN = "Invalid Token";
	String VALID_TOKEN = "Valid token for user ";
	String USERNAME_OR_PASSWORD_INVALID = "Username or Password should not be empty";

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/generatetoken")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    	if(loginRequest.getUsername().isEmpty() || loginRequest.getPassword().isEmpty()) {
    		 return new ResponseEntity(new ApiResponse(false, USERNAME_OR_PASSWORD_INVALID),
                     HttpStatus.BAD_REQUEST);
    	}
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        String jwt = tokenProvider.generateToken(loginRequest.getUsername(), authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/validatetoken")
    public ResponseEntity<?> getTokenByCredentials(@Valid @RequestBody ValidateTokenRequest validateToken) {
    	 String username = null;
    	 String jwt = validateToken.getToken();
         if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                username = tokenProvider.getUsernameFromJWT(jwt);
	          //If required we can have one more check here to load the user from LDAP server
             return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, VALID_TOKEN + username));
           }else {
        	   return new ResponseEntity(new ApiResponse(false, INVALID_TOKEN),
                       HttpStatus.BAD_REQUEST);
           }
         
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/validatetoken")
	public ResponseEntity<?> getTokenByCredentials(HttpServletRequest httpServletRequest) {
		String username = null;
		String authorizationHeader = httpServletRequest.getHeader(CommonConstants.TOKEN_HEADER); // JWT TOKEN retrieval from header
		DecodedJWT jwt;
		 String token = authorizationHeader
                 .substring("Bearer".length()).trim();
		if (token != null) {

			Algorithm algorithm = Algorithm.HMAC256(CommonConstants.SECRET_KEY);
			JWTVerifier verifier = JWT.require(algorithm).build();
			
			verifier.verify(token);

			jwt = JWT.decode(token);
			Claim usernameClaim = jwt.getClaim(CommonConstants.TOKENDAO_USER_NAME);
			Claim roleClain = jwt.getClaim(CommonConstants.TOKENDAO_ROLE);
			username = usernameClaim.asString();
			if (StringUtils.hasText(username)) {
				return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, VALID_TOKEN + username));
			}
			

		}
		return new ResponseEntity(new ApiResponse(false, INVALID_TOKEN), HttpStatus.BAD_REQUEST);
	}
}