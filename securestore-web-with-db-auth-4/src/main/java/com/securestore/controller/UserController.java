package com.securestore.controller;

import com.securestore.domain.CustomSecurityUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.RolesAllowed;


@Controller
@RequestMapping("/userPage")
public class UserController {

    @RequestMapping
    public String viewUserProfile() {
        return "user/profile";
    }


}
