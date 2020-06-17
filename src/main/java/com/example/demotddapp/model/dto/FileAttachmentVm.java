package com.example.demotddapp.model.dto;

import com.example.demotddapp.model.FileAttachment;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileAttachmentVm {

    private String name;

    private String fileType;

    public FileAttachmentVm(FileAttachment fileAttachment) {
        this.setName(fileAttachment.getName());
        this.setFileType(fileAttachment.getFileType());
    }
}
