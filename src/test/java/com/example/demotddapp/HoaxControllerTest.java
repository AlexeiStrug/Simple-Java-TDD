package com.example.demotddapp;

import com.example.demotddapp.model.FileAttachment;
import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.HoaxVm;
import com.example.demotddapp.repository.FileAttachmentRepository;
import com.example.demotddapp.repository.HoaxRepository;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.service.FileService;
import com.example.demotddapp.service.HoaxService;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.shared.error.ApiError;
import com.example.demotddapp.utils.TestPage;
import com.example.demotddapp.utils.TestUtils;
import com.example.demotddapp.utils.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

    private static final String API_V_1_HOAXES = "/api/v1/hoaxes";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    HoaxService hoaxService;

    @Autowired
    FileService fileService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HoaxRepository hoaxRepository;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    AppConfiguration appConfiguration;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void cleanup() throws IOException {
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        fileAttachmentRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_receiveOk() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        ResponseEntity<Object> responseEntity = postHoax(hoax, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_receiveHoaxVm() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        ResponseEntity<HoaxVm> responseEntity = postHoax(hoax, HoaxVm.class);

        assertThat(responseEntity.getBody().getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void test_postHoax_whenHoaxHasFileAttachmentAndUserIsAuth_fileAttachmentHoaxRelationIsUpdateInDb() throws IOException {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtils.createHoax();
        hoax.setFileAttachment(savedFile);
        ResponseEntity<HoaxVm> responseEntity = postHoax(hoax, HoaxVm.class);

        FileAttachment inDb = fileAttachmentRepository.findAll().get(0);

        assertThat(inDb.getHoax().getId()).isEqualTo(responseEntity.getBody().getId());
    }

    @Test
    public void test_postHoax_whenHoaxHasFileAttachmentAndUserIsAuth_hoaxFileAttachmentRelationIsUpdateInDb() throws IOException {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtils.createHoax();
        hoax.setFileAttachment(savedFile);
        ResponseEntity<HoaxVm> responseEntity = postHoax(hoax, HoaxVm.class);

        Hoax inDb = hoaxRepository.findById(responseEntity.getBody().getId()).get();

        assertThat(inDb.getFileAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void test_postHoax_whenHoaxHasFileAttachmentAndUserIsAuth_receiveHoaxVmWithAttachment() throws IOException {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtils.createHoax();
        hoax.setFileAttachment(savedFile);
        ResponseEntity<HoaxVm> responseEntity = postHoax(hoax, HoaxVm.class);

        assertThat(responseEntity.getBody().getFileAttachmentVm().getName()).isEqualTo(savedFile.getName());
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsUnauth_receiveUnauth() {
        Hoax hoax = TestUtils.createHoax();
        ResponseEntity<Object> responseEntity = postHoax(hoax, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsUnauth_receiveApiError() {
        Hoax hoax = TestUtils.createHoax();
        ResponseEntity<ApiError> responseEntity = postHoax(hoax, ApiError.class);

        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_hoaxSavedToDatabase() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        postHoax(hoax, Object.class);

        assertThat(hoaxRepository.count()).isEqualTo(1);
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_hoaxSavedToDatabaseWithTimestamp() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getTimestamp()).isNotNull();
    }

    @Test
    public void test_postHoax_whenHoaxContentIsNullAndUserIsUnauth_receiveBadRequest() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        ResponseEntity<Object> responseEntity = postHoax(hoax, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postHoax_whenHoaxContentIsLessThan10CharactersAndUserIsUnauth_receiveBadRequest() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent("123456789");
        ResponseEntity<Object> responseEntity = postHoax(hoax, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postHoax_whenHoaxContentIs5000CharactersAndUserIsUnauth_receiveOk() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        String verityString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        hoax.setContent(verityString);
        ResponseEntity<Object> responseEntity = postHoax(hoax, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_postHoax_whenHoaxContentMoreThan5000CharactersAndUserIsUnauth_receiveBadRequest() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        String verityString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        hoax.setContent(verityString);
        ResponseEntity<Object> responseEntity = postHoax(hoax, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_receiveApiErrorValidationError() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        ResponseEntity<ApiError> responseEntity = postHoax(hoax, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();

        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_hoaxSavedToDatabaseWithAuthUserInfo() {
        userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    @Transactional
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_hoaxCanBeAccessedFromUserEntity1way() {
        userService.save(TestUtils.createUser("user1"));
        TestTransaction.flagForCommit();
        TestTransaction.end();
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        postHoax(hoax, Object.class);

        TestTransaction.start();
        User userInDb = userRepository.findByUsername("user1");

        assertThat(userInDb.getHoaxes().size()).isEqualTo(1);
    }

    @Test
    public void test_postHoax_whenHoaxIsValidAndUserIsAuth_hoaxCanBeAccessedFromUserEntity2way() {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtils.createHoax();
        postHoax(hoax, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        User userInDb = entityManager.find(User.class, user.getId());

        assertThat(userInDb.getHoaxes().size()).isEqualTo(1);
    }

    @Test
    public void test_getHoaxes_whereThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> responseEntity = getHoaxes(new ParameterizedTypeReference<Object>() {
        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_getHoaxes_whereThereAreNoHoaxes_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> responseEntity = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void test_getHoaxes_whereThereAreHoaxes_receivePageWithItems() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<Object>> responseEntity = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {
        });

        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(3);
    }


    @Test
    public void test_getHoaxes_whereThereAreHoaxes_receivePageWithHoaxVM() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<HoaxVm>> responseEntity = getHoaxes(new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });
        HoaxVm storedHoax = responseEntity.getBody().getContent().get(0);
        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
    }


    @Test
    public void test_getHoaxesOfUser_whenUserExist_receiveOK() {
        User user = userService.save(TestUtils.createUser("user1"));
        ResponseEntity<Object> responseEntity = getHoaxesOfUser("user1", new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_getHoaxesOfUser_whenUserExist_receiveNotFound() {
        ResponseEntity<Object> responseEntity = getHoaxesOfUser("unknown-user", new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_getHoaxesOfUser_whenUserExist_receivePageWithZeroHoaxes() {
        User user = userService.save(TestUtils.createUser("user1"));
        ResponseEntity<TestPage<Object>> responseEntity = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<Object>>() {
        });

        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void test_getHoaxesOfUser_whenUserExist_receivePageWithHoaxVm() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<HoaxVm>> responseEntity = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });
        HoaxVm storedHoaxVm = responseEntity.getBody().getContent().get(0);

        assertThat(storedHoaxVm.getUser().getUsername()).isEqualTo("user1");
    }


    @Test
    public void test_getHoaxesOfUser_whenUserExistWithMultipleHoaxes_receivePageWithMatchingHoaxesCount() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<HoaxVm>> responseEntity = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });
        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void test_getHoaxesOfUser_whenMultipleUserExistWithMultipleHoaxes_receivePageWithMatchingHoaxesCount() {
        User userWith3Hoaxes = userService.save(TestUtils.createUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            hoaxService.save(TestUtils.createHoax(), userWith3Hoaxes);
        });

        User userWith5Hoaxes = userService.save(TestUtils.createUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            hoaxService.save(TestUtils.createHoax(), userWith5Hoaxes);
        });


        ResponseEntity<TestPage<HoaxVm>> responseEntity = getHoaxesOfUser(userWith5Hoaxes.getUsername(), new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });
        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void test_getOldHoaxes_whereThereIsNoHoaxes_receiveOk() {
        ResponseEntity<Object> responseEntity = getOldHoaxes(5, new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_getOldHoaxes_whereThereIsNoHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<Object>> responseEntity = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<Object>>() {
        });

        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void test_getOldHoaxes_whereThereIsNoHoaxes_receivePageWithHoaxVmItemsBeforeProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<HoaxVm>> responseEntity = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });

        assertThat(responseEntity.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void test_getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtils.createUser("user1"));
        ResponseEntity<Object> responseEntity = getOldHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void test_getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<Object>> responseEntity = getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<Object>>() {
        });

        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(3);
    }


    @Test
    public void test_getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePageWithHoaxVmItemsBeforeProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<TestPage<HoaxVm>> responseEntity = getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });

        assertThat(responseEntity.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void test_getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveNotFound() {
        ResponseEntity<Object> responseEntity = getOldHoaxesOfUser(5, "unknown-user", new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        userService.save(TestUtils.createUser("user2"));

        ResponseEntity<TestPage<HoaxVm>> responseEntity = getOldHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<TestPage<HoaxVm>>() {
        });

        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void test_getNewHoaxes_whenThereAreNoHoaxes_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<List<Object>> responseEntity = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<Object>>() {
        });

        assertThat(responseEntity.getBody().size()).isEqualTo(1);
    }

    @Test
    public void test_getNewHoaxes_whenThereAreNoHoaxes_receiveListOfHoaxVmAfterProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<List<HoaxVm>> responseEntity = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<HoaxVm>>() {
        });

        assertThat(responseEntity.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void test_getNewHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtils.createUser("user1"));
        ResponseEntity<Object> responseEntity = getNewHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_getNewHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<List<Object>> responseEntity = getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<Object>>() {
        });

        assertThat(responseEntity.getBody().size()).isEqualTo(1);
    }

    @Test
    public void test_getNewHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receiveListWithHoaxVmItemsBeforeProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<List<HoaxVm>> responseEntity = getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<HoaxVm>>() {
        });

        assertThat(responseEntity.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void test_getNewHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveNotFound() {
        ResponseEntity<Object> responseEntity = getNewHoaxesOfUser(5, "unknown-user", new ParameterizedTypeReference<Object>() {
        });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_getNewHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receiveListWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        userService.save(TestUtils.createUser("user2"));

        ResponseEntity<List<HoaxVm>> responseEntity = getNewHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<List<HoaxVm>>() {
        });

        assertThat(responseEntity.getBody().size()).isEqualTo(0);
    }


    @Test
    public void test_getNewHoaxCount_whenThereAreNoHoaxes_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<Map<String, Long>> responseEntity = getNewHoaxCount(fourth.getId(), new ParameterizedTypeReference<Map<String, Long>>() {
        });

        assertThat(responseEntity.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void test_getNewHoaxCountOfUser_whenThereAreNoHoaxes_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtils.createUser("user1"));
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);
        Hoax fourth = hoaxService.save(TestUtils.createHoax(), user);
        hoaxService.save(TestUtils.createHoax(), user);

        ResponseEntity<Map<String, Long>> responseEntity = getNewHoaxCountOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<Map<String, Long>>() {
        });

        assertThat(responseEntity.getBody().get("count")).isEqualTo(1);
    }


    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P5ssword"));
    }

    private <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_V_1_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_V_1_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getOldHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/v1/users/" + username + "/hoaxes/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getNewHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/v1/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getNewHoaxCount(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_V_1_HOAXES + "/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getNewHoaxCountOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/v1/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getHoaxes(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_V_1_HOAXES, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getHoaxesOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/v1/users/" + username + "/hoaxes";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_V_1_HOAXES, hoax, responseType);
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("test-png.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        return new MockMultipartFile("test-png.png", fileAsByte);
    }

}
