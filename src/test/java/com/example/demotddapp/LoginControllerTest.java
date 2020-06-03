package com.example.demotddapp;

import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.shared.error.ApiError;
import com.example.demotddapp.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

    private static final String API_V_1_LOGIN = "/api/v1/login";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @Test
    public void test_postLogin_withoutUserCredentials_receiveUnauthorized() {
        ResponseEntity<Object> responseEntity = login(Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_postLogin_withUserIncorrectCredentials_receiveUnauthorized() {
        authenticate();
        ResponseEntity<Object> responseEntity = login(Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_postLogin_withoutUserCredentials_receiveApiError() {
        ResponseEntity<ApiError> responseEntity = login(ApiError.class);
        assertThat(responseEntity.getBody().getUrl()).isEqualTo(API_V_1_LOGIN);
    }

    @Test
    public void test_postLogin_withoutUserCredentials_receiveApiErrorWithoutValidationError() {
        ResponseEntity<String> responseEntity = login(String.class);
        assertThat(responseEntity.getBody().contains("validationErrors")).isFalse();
    }

    @Test
    public void test_postLogin_withUserIncorrectCredentials_receiveUnauthorizedWithoutWwwAuthenticationHeader() {
        authenticate();
        ResponseEntity<Object> responseEntity = login(Object.class);
        assertThat(responseEntity.getHeaders().containsKey("WWW-Authenticate")).isFalse();
    }

    @Test
    public void test_postLogin_withValidCredentials_receiveOk() {
        userService.save(TestUtils.createUser());
        authenticate();

        ResponseEntity<Object> responseEntity = login(Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void test_postLogin_withValidCredentials_receiveLoggedUserId() {
        User user = userService.save(TestUtils.createUser());
        authenticate();

        ResponseEntity<Map<String, Object>> responseEntity = login(new ParameterizedTypeReference<Map<String, Object>>() {
        });
        Map<String, Object> body = responseEntity.getBody();
        Integer id = (Integer) body.get("id");

        assertThat(id).isEqualTo(user.getId());

    }

    @Test
    public void test_postLogin_withValidCredentials_receiveLoggedUserImage() {
        User user = userService.save(TestUtils.createUser());
        authenticate();

        ResponseEntity<Map<String, Object>> responseEntity = login(new ParameterizedTypeReference<Map<String, Object>>() {
        });
        Map<String, Object> body = responseEntity.getBody();
        String image = (String) body.get("image");

        assertThat(image).isEqualTo(user.getImage());

    }

    @Test
    public void test_postLogin_withValidCredentials_receiveLoggedUserDisplayName() {
        User user = userService.save(TestUtils.createUser());
        authenticate();

        ResponseEntity<Map<String, Object>> responseEntity = login(new ParameterizedTypeReference<Map<String, Object>>() {
        });
        Map<String, Object> body = responseEntity.getBody();
        String displayName = (String) body.get("displayName");

        assertThat(displayName).isEqualTo(user.getDisplayName());

    }

    @Test
    public void test_postLogin_withValidCredentials_receiveLoggedUserUsername() {
        User user = userService.save(TestUtils.createUser());
        authenticate();

        ResponseEntity<Map<String, Object>> responseEntity = login(new ParameterizedTypeReference<Map<String, Object>>() {
        });
        Map<String, Object> body = responseEntity.getBody();
        String username = (String) body.get("username");

        assertThat(username).isEqualTo(user.getUsername());

    }


    @Test
    public void test_postLogin_withValidCredentials_notReceiveLoggedUserPassword() {
        userService.save(TestUtils.createUser());
        authenticate();

        ResponseEntity<Map<String, Object>> responseEntity = login(new ParameterizedTypeReference<Map<String, Object>>() {
        });
        Map<String, Object> body = responseEntity.getBody();
        String password = (String) body.get("password");

        assertThat(password).isNull();

    }

    private void authenticate() {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor("test-user", "P5ssword"));
    }

    public <T> ResponseEntity<T> login(Class<T> response) {
        return testRestTemplate.postForEntity(API_V_1_LOGIN, null, response);
    }

    public <T> ResponseEntity<T> login(ParameterizedTypeReference<T> response) {
        return testRestTemplate.exchange(API_V_1_LOGIN, HttpMethod.POST, null, response);
    }
}
