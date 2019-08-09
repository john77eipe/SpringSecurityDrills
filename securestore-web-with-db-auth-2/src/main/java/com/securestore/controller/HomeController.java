package com.securestore.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HomeController {

    @RequestMapping("/")
    public String viewHome() {
        return "index";
    }
    
    @RequestMapping("/special")
    @PreAuthorize("hasRole('HRADMIN')")
    public @ResponseBody String jsonResponse() {
    	return "{'data':'hello'}";
    }
}
