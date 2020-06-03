package com.example.demotddapp.controller;

import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.UserVM;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.shared.GenericResponse;
import com.example.demotddapp.shared.error.ApiError;
import com.example.demotddapp.utils.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


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


    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, "Validation error", request.getRequestURI());
        BindingResult bindingResult = exception.getBindingResult();
        Map<String, String> validationResult = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            validationResult.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        apiError.setValidationErrors(validationResult);
        return apiError;
    }

}
