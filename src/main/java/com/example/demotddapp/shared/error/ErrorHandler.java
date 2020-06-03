package com.example.demotddapp.shared.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@RestController
public class ErrorHandler implements ErrorController {

    @Autowired
    ErrorAttributes errorAttributes;

    @RequestMapping("/error")
    ApiError handleError(WebRequest webRequest) {
        Map<String, Object> map = errorAttributes.getErrorAttributes(webRequest, true);

        String message = (String) map.get("message");
        String url = (String) map.get("path");
        int status = (Integer) map.get("status");

        return new ApiError(status, message, url);

    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
