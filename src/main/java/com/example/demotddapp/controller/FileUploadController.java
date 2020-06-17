package com.example.demotddapp.controller;

import com.example.demotddapp.model.FileAttachment;
import com.example.demotddapp.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class FileUploadController {

    @Autowired
    FileService fileService;

    @PostMapping("/hoaxes/upload")
    FileAttachment uploadForHoax(MultipartFile file) {
        return fileService.saveAttachment(file);
    }
}
