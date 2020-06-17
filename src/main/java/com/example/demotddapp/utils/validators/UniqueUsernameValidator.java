package com.example.demotddapp.utils.validators;

import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.utils.annotation.UniqueUsername;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {


    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        User find = userRepository.findByUsername(value);
        return find == null;
    }
}
