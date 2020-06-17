package com.example.demotddapp;

import com.example.demotddapp.utils.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaticResourceTest {

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    MockMvc mockMvc;

    @After
    public void cleanUp() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void test_checkStaticFolder_whenAppIsInit_uploadFolderMustExist() {
        File uploadFolder = new File(appConfiguration.getUploadPath());
        boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();
        assertThat(uploadFolderExist).isTrue();
    }

    @Test
    public void test_checkStaticFolder_whenAppIsInit_profileImageSubFolderMustExist() {
        String profileImageFolderPath = appConfiguration.getFullProfileImagesPath();
        File profileImageFolder = new File(profileImageFolderPath);
        boolean uploadFolderExist = profileImageFolder.exists() && profileImageFolder.isDirectory();
        assertThat(uploadFolderExist).isTrue();
    }

    @Test
    public void test_checkStaticFolder_whenAppIsInit_attachmentSubFolderMustExist() {
        String attachmentFolderPath = appConfiguration.getFullAttachmentsPath();
        File attachmentsFolder = new File(attachmentFolderPath);
        boolean uploadFolderExist = attachmentsFolder.exists() && attachmentsFolder.isDirectory();
        assertThat(uploadFolderExist).isTrue();
    }

    @Test
    public void test_getStaticFile_whenImageExistInProfileUploadFolder_receiveOk() throws Exception {
        String filename = "profile-picture.png";
        File source = new ClassPathResource("profile.png").getFile();

        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + filename);
        FileUtils.copyFile(source, target);

        mockMvc.perform(get("/images/" + appConfiguration.getProfileImagesFolder() + "/" + filename)).andExpect(status().isOk());
    }

    @Test
    public void test_getStaticFile_whenImageExistInAttachmentFolder_receiveOk() throws Exception {
        String filename = "profile-picture.png";
        File source = new ClassPathResource("profile.png").getFile();

        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + filename);
        FileUtils.copyFile(source, target);

        mockMvc.perform(get("/images/" + appConfiguration.getAttachmentsFolder() + "/" + filename)).andExpect(status().isOk());
    }

    @Test
    public void test_getStaticFile_whenImageDoesNotExist_receiveNotFound() throws Exception {
        mockMvc.perform(get("/images/" + appConfiguration.getAttachmentsFolder() + "/there-is-no-such-image.png")).andExpect(status().isNotFound());
    }

    @Test
    public void test_getStaticFile_whenImageExistInAttachmentFolder_receiveOkWithCacheHeaders() throws Exception {
        String filename = "profile-picture.png";
        File source = new ClassPathResource("profile.png").getFile();

        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + filename);
        FileUtils.copyFile(source, target);

        MvcResult mvcResult = mockMvc.perform(get("/images/" + appConfiguration.getAttachmentsFolder() + "/" + filename)).andReturn();

        String cacheControl = mvcResult.getResponse().getHeaderValue("Cache-control").toString();
        assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
    }
}
