package com.securestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/adminPage")
public class AdminController {

    @RequestMapping
    public String viewManageUsers() {
        return "/admin/manage-users";
    }
}
