package com.securestore.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class AdminController {

    @ResponseBody
    @GetMapping("/admin")
    @Secured("ROLE_hradmin")
    public String viewManageUsers() {
        return "manage-users";
    }
    
}
