package com.example.demotddapp.controller;

import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.UserVM;
import com.example.demotddapp.utils.annotation.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {


    @PostMapping("/api/v1/login")
    UserVM handleLogin(@CurrentUser User loggedInUser) {
        return new UserVM(loggedInUser);
    }
}
