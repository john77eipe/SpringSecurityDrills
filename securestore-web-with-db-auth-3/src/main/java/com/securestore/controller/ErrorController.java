package com.securestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/generic-error")
public class ErrorController {

    @RequestMapping
    public String errorPage() {
        return "error";
    }
}
