package com.example.demotddapp;

import com.example.demotddapp.model.FileAttachment;
import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.FileAttachmentRepository;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.utils.TestUtils;
import com.example.demotddapp.utils.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

    private static final String API_V_1_HOAXES_UPLOAD = "/api/v1/hoaxes/upload";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Before
    public void init() throws IOException {
        userRepository.deleteAll();
        fileAttachmentRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void test_uploadFile_withImageFromAuthUser_receiveOk() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        ResponseEntity<Object> responseEntity = uploadFile(getRequestEntity(), Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_uploadFile_withImageFromUnauthUser_receiveUnauth() {
        ResponseEntity<Object> responseEntity = uploadFile(getRequestEntity(), Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_uploadFile_withImageFromAuthUser_receiveFileAttachmentWithDate() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> responseEntity = uploadFile(getRequestEntity(), FileAttachment.class);

        assertThat(responseEntity.getBody().getDate()).isNotNull();
    }

    @Test
    public void test_uploadFile_withImageFromAuthUser_receiveFileAttachmentWithRandomName() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> responseEntity = uploadFile(getRequestEntity(), FileAttachment.class);

        assertThat(responseEntity.getBody().getName()).isNotNull();
        assertThat(responseEntity.getBody().getName()).isNotEqualTo("test-png.png");
    }

    @Test
    public void test_uploadFile_withImageFromAuthUser_imageSavedToFolder() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> responseEntity = uploadFile(getRequestEntity(), FileAttachment.class);

        String imagePath = appConfiguration.getFullAttachmentsPath() + "/" + responseEntity.getBody().getName();
        File storedImage = new File(imagePath);

        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void test_uploadFile_withImageFromAuthUser_fileAttachmentSavedToDb() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        uploadFile(getRequestEntity(), FileAttachment.class);

        assertThat(fileAttachmentRepository.count()).isEqualTo(1);
    }

    @Test
    public void test_uploadFile_withImageFromAuthUser_fileAttachmentStoredWithFileType() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        uploadFile(getRequestEntity(), FileAttachment.class);
        FileAttachment storedFiles = fileAttachmentRepository.findAll().get(0);

        assertThat(storedFiles.getFileType()).isEqualTo("image/png");
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P5ssword"));
    }

    private <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
        return testRestTemplate.exchange(API_V_1_HOAXES_UPLOAD, HttpMethod.POST, requestEntity, responseType);
    }

    private HttpEntity<MultiValueMap<String, Object>> getRequestEntity() {
        ClassPathResource imageResource = new ClassPathResource("test-png.png");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }
}
