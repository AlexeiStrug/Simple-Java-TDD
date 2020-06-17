package com.example.demotddapp;

import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.UserUpdateVM;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.service.UserService;
import com.example.demotddapp.shared.GenericResponse;
import com.example.demotddapp.shared.error.ApiError;
import com.example.demotddapp.utils.TestPage;
import com.example.demotddapp.utils.TestUtils;
import com.example.demotddapp.utils.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

    private static final String API_V_1_USERS = "/api/v1/users";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @After
    public void cleanDirectory() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    /* ---------------------- Start -> User tests ------------------------- */
    @Test
    public void test_postUser_whenUserIsValid_receiveOk() {
        User user = TestUtils.createUser();
        ResponseEntity<Object> responseEntity = postSignup(user, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_postUser_whenUserIsValid_saveUserToDatabase() {
        User user = TestUtils.createUser();
        postSignup(user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void test_postUser_whenUserIsValid_receiveSuccessMessage() {
        User user = TestUtils.createUser();
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getBody().getMessage()).isNotNull();
    }

    @Test
    public void test_postUser_whenUserIsValid_passwordHashedInDb() {
        User user = TestUtils.createUser();
        postSignup(user, Object.class);
        List<User> users = userRepository.findAll();
        User inDb = users.get(0);
        assertThat(inDb.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    public void test_postUser_whenUserHasNullUsername_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setUsername(null);
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserIsInvalid_receiveApiError() {
        User user = TestUtils.createUser();
        user.setPassword("123456789");
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        assertThat(responseEntity.getBody().getUrl()).isEqualTo(API_V_1_USERS);
    }

    @Test
    public void test_postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors() {
        User user = new User();
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        assertThat(responseEntity.getBody().getValidationErrors().size()).isEqualTo(3);
    }
    /* ---------------------- End -> User tests ------------------------- */

    /* ---------------------- Start -> Username tests ------------------------- */
    @Test
    public void test_postUser_whenUserHasUsernameLessThanRequired_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setUsername("abc");
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasUsernameExceedLengthLimit_receiveBadRequest() {
        User user = TestUtils.createUser();
        String value256char = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(value256char);
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasNullUsername_receiveMessageErrorOfNullUsername() {
        User user = TestUtils.createUser();
        user.setUsername(null);
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username can not be null.");
    }

    @Test
    public void test_postUser_whenUserHasInvalidLengthUsername_receiveMessageErrorOfSizeError() {
        User user = TestUtils.createUser();
        user.setUsername("abc");
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
    }

    @Test
    public void test_postUser_whenAnotherUserHasTheSameUsername_receiveBadRequest() {
        userRepository.save(TestUtils.createUser());
        User user = TestUtils.createUser();
        ResponseEntity<Object> responseEntity = postSignup(user, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenAnotherUserHasTheSameUsername_receiveMessageOfDuplicateUsername() {
        userRepository.save(TestUtils.createUser());
        User user = TestUtils.createUser();
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
    }

    /* ---------------------- End -> Username tests ------------------------- */


    /* ---------------------- Start -> DisplayName tests ------------------------- */
    @Test
    public void test_postUser_whenUserHasNullDisplayName_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setDisplayName(null);
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasDisplayNameLessThanRequired_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setDisplayName("abc");
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasDisplayNameExceedLengthLimit_receiveBadRequest() {
        User user = TestUtils.createUser();
        String value256char = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(value256char);
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    /* ---------------------- End -> DisplayName tests ------------------------- */

    /* ---------------------- Start -> Password tests ------------------------- */
    @Test
    public void test_postUser_whenUserHasNullPassword_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setPassword(null);
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasPasswordLessThanRequired_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setPassword("P5sswor");
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasPasswordExceedLengthLimit_receiveBadRequest() {
        User user = TestUtils.createUser();
        String value256char = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setPassword(value256char);
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasPasswordWithAllLowerCase_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setPassword("alllowercase");
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasPasswordWithAllUpperCase_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setPassword("ALLUPPERCASE");
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasPasswordWithAllNumber_receiveBadRequest() {
        User user = TestUtils.createUser();
        user.setPassword("123456789");
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_postUser_whenUserHasNullPassword_receiveMessageErrorOfNull() {
        User user = TestUtils.createUser();
        user.setPassword(null);
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Cannot be null.");
    }

    @Test
    public void test_postUser_whenUserHasInvalidPasswordPatter_receiveMessageErrorOfPasswordPatternError() {
        User user = TestUtils.createUser();
        user.setPassword("alllowercase");
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
    }
    /* ---------------------- End -> Password tests ------------------------- */

    /* ---------------------- Start -> CRUD Users tests ------------------------- */

    @Test
    public void test_getUsers_whenThereAreNoUsersInDb_receiveOk() {
        ResponseEntity<Object> responseEntity = testRestTemplate.getForEntity(API_V_1_USERS, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_getUsers_whenThereAreNoUsersInDb_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void test_getUsers_whenThereIsAUsersInDb_receivePageWithUser() {
        userRepository.save(TestUtils.createUser());
        ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    public void test_getUsers_whenThereIsAUsersInDb_receiveUserWithoutPassword() {
        userRepository.save(TestUtils.createUser());
        ResponseEntity<TestPage<Map<String, Object>>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
        });
        Map<String, Object> mapEntity = responseEntity.getBody().getContent().get(0);
        assertThat(mapEntity.containsKey("password")).isFalse();
    }


    @Test
    public void test_getUsers_whenPageIsRequestedFor3ItemsPerPageWhereTheDatabaseHas20Users_receive3Users() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i)
                .map(TestUtils::createUser)
                .forEach(userRepository::save);
        String path = API_V_1_USERS + "?page=0&size=3";
        ResponseEntity<TestPage<Map<String, Object>>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
        });
        assertThat(responseEntity.getBody().getSize()).isEqualTo(3);
    }

    @Test
    public void test_getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
        ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void test_getUsers_whenPageSizeIsGreaterThan100_receivePageSizeAs100() {
        String path = API_V_1_USERS + "?size=500";
        ResponseEntity<TestPage<Object>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getSize()).isEqualTo(100);
    }

    @Test
    public void test_getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
        String path = API_V_1_USERS + "?size=-1";
        ResponseEntity<TestPage<Object>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void test_getUsers_whenPageIsNegative_receivePageSizeAs10() {
        String path = API_V_1_USERS + "?page=-1";
        ResponseEntity<TestPage<Object>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(responseEntity.getBody().getNumber()).isEqualTo(0);
    }

//    @Test
//    public void test_getUsers_whenUserLoggedIn_receivePageWithoutLoggedInUser() {
//        IntStream.rangeClosed(1, 3).mapToObj(i -> "test-user-" + i)
//                .map(TestUtils::createUser)
//                .forEach(userRepository::save);
//
//        authenticate("user1");
//        ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
//        });
//        assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(2);
//    }

    @Test
    public void test_getUsersByUsername_whenUserExist_receiveOk() {
        String username = "test-user";
        userRepository.save(TestUtils.createUser(username));
        ResponseEntity<Object> responseEntity = getUser(username, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_getUsersByUsername_whenUserExist_receiveUserWithoutPassword() {
        String username = "test-user";
        userRepository.save(TestUtils.createUser(username));
        ResponseEntity<String> responseEntity = getUser(username, String.class);
        assertThat(responseEntity.getBody().contains("password")).isFalse();
    }

    @Test
    public void test_getUsersByUsername_whenUserNotExist_receiveNotFound() {
        ResponseEntity<Object> responseEntity = getUser("unknown-user", Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_getUsersByUsername_whenUserNotExist_receiveApiError() {
        ResponseEntity<ApiError> responseEntity = getUser("unknown-user", ApiError.class);
        assertThat(responseEntity.getBody().getMessage().contains("unknown-user")).isTrue();
    }

    @Test
    public void test_putUser_whenUnauthorizedUserSendsTheRequest_receive401() {
        ResponseEntity<Object> responseEntity = putUser(123, null, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveForbidden() {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<Object> responseEntity = putUser(anotherUserId, null, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void test_putUser_whenUnauthorizedUserSendsTheRequest_receiveApiError() {
        ResponseEntity<ApiError> responseEntity = putUser(123, null, ApiError.class);
        assertThat(responseEntity.getBody().getUrl()).contains("users/123");
    }

    @Test
    public void test_putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveApiError() {
        User user = userService.save(TestUtils.createUser());
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<ApiError> responseEntity = putUser(anotherUserId, null, ApiError.class);
        assertThat(responseEntity.getBody().getUrl()).contains("users/" + anotherUserId);
    }

    @Test
    public void test_putUser_whenValidRequestBodyFromAuthUser_receiveOk() {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);

        ResponseEntity<ApiError> responseEntity = putUser(user.getId(), requestEntity, ApiError.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_putUser_whenValidRequestBodyFromAuthUser_receiveDisplayNameUpdated() {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        putUser(user.getId(), requestEntity, Object.class);

        User userInDb = userRepository.findByUsername("user1");
        assertThat(userInDb.getDisplayName()).isEqualTo(updatedUser.getDisplayName());
    }

    @Test
    public void test_putUser_whenValidRequestBodyFromAuthUser_receiveUpdatedVmWithUpdatedDisplayName() {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateVM> responseEntity = putUser(user.getId(), requestEntity, UserUpdateVM.class);

        assertThat(responseEntity.getBody().getDisplayName()).isEqualTo(updatedUser.getDisplayName());
    }

    @Test
    public void test_putUser_whenValidRequestBodyWithSupportedImageFromAuthUser_receiveUpdatedVmWithRandomImageName() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createUserUpdateVM();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateVM> responseEntity = putUser(user.getId(), requestEntity, UserUpdateVM.class);

        assertThat(responseEntity.getBody().getImage()).isNotEqualTo("profile-image.png");
    }

    @Test
    public void test_putUser_whenValidRequestBodyWithSupportedImageFromAuthUser_imageStoreUnderProfileFolder() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createUserUpdateVM();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateVM> responseEntity = putUser(user.getId(), requestEntity, UserUpdateVM.class);

        String storedImageName = responseEntity.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
        File storedImage = new File(profilePicturePath);

        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithNullDisplayNameFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = new UserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> responseEntity = putUser(user.getId(), requestEntity, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithLessSizeThanMinSizeDisplayNameFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("abc");

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> responseEntity = putUser(user.getId(), requestEntity, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithMoreSizeThanMaxSizeDisplayNameFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = new UserUpdateVM();
        String value256char = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        updatedUser.setDisplayName(value256char);


        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> responseEntity = putUser(user.getId(), requestEntity, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithJpgImageFromAuthUser_receiveOk() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createUserUpdateVM();
        String imageString = readFileToBase64("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateVM> responseEntity = putUser(user.getId(), requestEntity, UserUpdateVM.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithGifImageFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createUserUpdateVM();
        String imageString = readFileToBase64("test-gif.gif");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> responseEntity = putUser(user.getId(), requestEntity, Object.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithTxtImageFromAuthUser_receiveValidationErrorForProfileImage() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createUserUpdateVM();
        String imageString = readFileToBase64("test-txt.txt");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<ApiError> responseEntity = putUser(user.getId(), requestEntity, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("image")).isEqualTo("Only PNG or JPG files are allowed");
    }

    @Test
    public void test_putUser_withInvalidRequestBodyWithJpgImageForUserWhoHasImage_removeOldImageFromStorage() throws IOException {
        User user = userService.save(TestUtils.createUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createUserUpdateVM();
        String imageString = readFileToBase64("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateVM> responseEntity = putUser(user.getId(), requestEntity, UserUpdateVM.class);

        putUser(user.getId(), requestEntity, UserUpdateVM.class);

        String storedImage = responseEntity.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImage;
        File storedFile = new File(profilePicturePath);
        assertThat(storedFile.exists()).isTrue();
    }

    /* ---------------------- End -> CRUD Users tests ------------------------- */

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P5ssword"));
    }

    private UserUpdateVM createUserUpdateVM() {
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("newDisplayName");
        return updatedUser;
    }

    private String readFileToBase64(String filename) throws IOException {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
        String imageString = Base64.getEncoder().encodeToString(imageArr);
        return imageString;
    }

    private <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_V_1_USERS, request, response);
    }

    private <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> parameterizedTypeReference) {
        return testRestTemplate.exchange(API_V_1_USERS, HttpMethod.GET, null, parameterizedTypeReference);
    }

    private <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> parameterizedTypeReference) {
        return testRestTemplate.exchange(path, HttpMethod.GET, null, parameterizedTypeReference);
    }

    private <T> ResponseEntity<T> getUser(String username, Class<T> response) {
        String path = API_V_1_USERS + "/" + username;
        return testRestTemplate.getForEntity(path, response);
    }

    private <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> response) {
        String path = API_V_1_USERS + "/" + id;
        return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, response);
    }
}
