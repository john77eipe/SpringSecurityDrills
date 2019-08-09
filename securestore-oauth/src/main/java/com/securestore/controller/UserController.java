package com.securestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/userPage")
public class UserController {

    @RequestMapping("/list")
    public String viewUserProfile() {
        return "user/profile";
    }
}
