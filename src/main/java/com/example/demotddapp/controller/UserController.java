package com.example.demotddapp.controller;

import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.UserUpdateVM;
import com.example.demotddapp.model.dto.UserVM;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.shared.GenericResponse;
import com.example.demotddapp.utils.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {
        userService.save(user);
        return new GenericResponse("User saved");
    }

    @GetMapping("/users")
    Page<UserVM> getUsers(@CurrentUser User loggedUser, Pageable pageable) {
        return userService.getUsers(loggedUser, pageable).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUser(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return new UserVM(user);
    }

    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserUpdateVM updateUser(@PathVariable Long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdateVM) {
        User updated = userService.update(id, userUpdateVM);
        return new UserUpdateVM(updated);
    }
}
