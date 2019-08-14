package com.securestore.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Controller
public class ClientController {

    @RequestMapping("/")
    public String viewHome() {
        return "index";
    }

    @RequestMapping("/its-me")
    public @ResponseBody String home(Principal principal) {
        return "The principal's name is: " + principal.getName();
    }

    @RequestMapping("/securedPage")
    public String viewSecurePage() {
        return "securedPage";
    }


}