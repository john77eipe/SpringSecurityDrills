package com.securestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminPage")
public class AdminController {

    @RequestMapping("/list")
    public String viewUserProfile() {
        return "admin/manage-users";
    }
}