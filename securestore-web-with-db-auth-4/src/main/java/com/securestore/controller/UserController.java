package com.securestore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/userPage")
public class UserController {

    @RequestMapping
    public String viewUserProfile() {
        return "user/profile";
    }

    @RequestMapping(value = "/user/test/{id}")
//    @PreAuthorize("hasRole('USER')")
    public @ResponseBody
    String test(@PathVariable("id") long id) {
        System.out.println("Fetching User with id " + id);
        return "{'a':'1'}";
    }

}
