package com.securestore.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class ClientController {


    @RequestMapping("/its-me")
    public String home(Principal principal) {
        return "The principal's name is: " + principal.getName();
    }


}