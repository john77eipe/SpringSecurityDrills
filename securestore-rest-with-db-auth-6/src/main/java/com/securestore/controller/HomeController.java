package com.securestore.controller;

import com.securestore.domain.CustomSecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.RolesAllowed;


@Controller
public class HomeController {

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
        System.out.println(CustomSecurityUser.displayEncryptedPassword());
        return "home";
    }

}
