package com.example.demotddapp.shared;

import com.example.demotddapp.shared.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

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
