package com.example.demotddapp;

import com.example.demotddapp.service.FileService;
import com.example.demotddapp.utils.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class FileServiceTest {

    FileService fileService;

    AppConfiguration appConfiguration;

    @Before
    public void init() {
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileService(appConfiguration);

        new File(appConfiguration.getUploadPath()).mkdir();
        new File(appConfiguration.getFullProfileImagesPath()).mkdir();
        new File(appConfiguration.getFullAttachmentsPath()).mkdir();
    }

    @After
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void test_detectType_whenPngFileProvided_returnsImagePng() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-png.png");
        byte[] fileArr = FileUtils.readFileToByteArray(resource.getFile());
        String fileType = fileService.detectType(fileArr);
        assertThat(fileType).isEqualToIgnoringCase("image/png");
    }


}
