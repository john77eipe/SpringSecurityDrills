package com.securestore.resource;

import com.securestore.config.JwtUtils;
import com.securestore.domain.Authorities;
import com.securestore.domain.CustomSecurityUser;
import com.securestore.domain.UserAccount;
import com.securestore.service.impl.UserAccountDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/auth")
public class UserAuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserAccountDetailsServiceImpl userAccountDetailsService;


    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomSecurityUser userDetails = (CustomSecurityUser) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

        // Create new user's account
        System.out.println("Creating User " + signupRequest);

        if (userAccountDetailsService.isUserExistByUsername(signupRequest.getUsername())) {
            System.out.println("A User with username: " + signupRequest.getUsername() + " already exist");
            return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setName(signupRequest.getName());
        userAccount.setUsername(signupRequest.getUsername());
        userAccount.setAge(signupRequest.getAge());
        userAccount.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Set<String> roles = signupRequest.getRole();
        Set<Authorities> authorities = new HashSet<>();
        for(String role: roles) {
            Authorities authority = new Authorities();
            authority.setAuthority(role);
            authority.setUserAccount(userAccount);
            authorities.add(authority);
        }
        userAccount.setAuthorities(authorities);

        userAccountDetailsService.saveUser(userAccount);

        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }
}
