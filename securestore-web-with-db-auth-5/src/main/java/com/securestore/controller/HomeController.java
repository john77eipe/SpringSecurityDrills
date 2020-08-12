package com.securestore.controller;

import com.securestore.domain.CustomSecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.securestore.domain.Authorities;
import com.securestore.domain.CustomSecurityUser;
import com.securestore.domain.UserAccount;
import com.securestore.dto.SignupRequest;
import com.securestore.service.impl.UserAccountDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.security.RolesAllowed;
import java.util.HashSet;
import java.util.Set;


@Controller
public class HomeController {

    @Autowired
    UserAccountDetailsServiceImpl userAccountDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping("/")
    public String viewHome() {
        return "home";
    }

    @RequestMapping("/loginPage")
    public String loginPage() {
        return "login_form";
    }

    @RequestMapping(value = "/logUserProfileDetails")
    @RolesAllowed("ROLE_USER")
    public String logUserDetails(Authentication authentication) {
        CustomSecurityUser customSecurityUser = (CustomSecurityUser) authentication.getPrincipal();
        System.out.println("User has authorities: " + customSecurityUser.getAuthorities());
        System.out.println("User has name: "+ customSecurityUser.getName());
        System.out.println("User has username: "+ customSecurityUser.getUsername());
        System.out.println("User has age: "+ customSecurityUser.getAge());
        System.out.println("User has domain: "+customSecurityUser.getDomain());
        System.out.println(CustomSecurityUser.displayEncryptedPassword());
        return "home";
    }

    @RequestMapping(value = "/register/form", method = RequestMethod.GET)
    public String registerForm() {
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(SignupRequest signupRequest) {
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

        Authorities authority = new Authorities();
        authority.setAuthority("ROLE_USER"); //default role
        Set<Authorities> authorities = new HashSet<>();
        authority.setUserAccount(userAccount);
        authorities.add(authority);
        userAccount.setAuthorities(authorities);

        userAccountDetailsService.saveUser(userAccount);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }
}
