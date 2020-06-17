package com.example.demotddapp.utils.validators;

import com.example.demotddapp.service.FileService;
import com.example.demotddapp.utils.annotation.ProfileImage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class ProfileImageValidator implements ConstraintValidator<ProfileImage, String> {

    @Autowired
    FileService fileService;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        byte[] decodedBytes = Base64.getDecoder().decode(s);
        String fileType = fileService.detectType(decodedBytes);
        if (fileType.equalsIgnoreCase("image/png") || fileType.equalsIgnoreCase("image/jpeg")) {
            return true;
        }
        return false;
    }

}
