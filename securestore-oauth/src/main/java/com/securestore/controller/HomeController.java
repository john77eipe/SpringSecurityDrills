package com.securestore.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HomeController {

    @RequestMapping("/")
    public String viewHome() {
        return "index";
    }

    @RequestMapping("/loginPage")
    public String loginPage() {
        return "login_form";
    }

}
