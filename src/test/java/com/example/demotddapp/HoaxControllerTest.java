package com.example.demotddapp;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.HoaxVm;
import com.example.demotddapp.repository.HoaxRepository;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.service.HoaxService;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.shared.error.ApiError;
import com.example.demotddapp.utils.TestPage;
import com.example.demotddapp.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

    public static final String API_V_1_HOAXES = "/api/v1/hoaxes";
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    HoaxService hoaxService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HoaxRepository hoaxRepository;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void cleanup() {
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
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

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P5ssword"));
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

}
