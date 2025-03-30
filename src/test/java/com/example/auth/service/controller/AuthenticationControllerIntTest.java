package com.example.auth.service.controller;

import com.example.auth.service.common.dto.ResponsePayload;
import com.example.auth.service.dto.CreateUserRequest;
import com.example.auth.service.dto.LoginResponse;
import com.example.auth.service.dto.LoginUserRequest;
import com.example.auth.service.model.User;
import com.example.auth.service.repository.UserRepository;
import com.example.auth.service.service.AuthenticationService;
import com.example.auth.service.service.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.example.auth.service.TestUtils.*;
import static com.example.auth.service.TestUtils.TestUser.*;
import static com.example.auth.service.common.constant.ApiConstant.BEARER;
import static com.example.auth.service.constant.UriConstant.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerIntTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ModelMapper mapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void teardown() {
        userRepository.deleteAll();
    }

    private static final Path basePath = Paths.get("src", "test", "resources", "expected_output", "authenticate");

    private String generateToken() {
        return jwtService.generateToken(buildUserDetails(USERNAME, EMAIL));
    }

    private static User buildUserDetails(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(PASSWORD);
        user.setEmail(email);
        return user;
    }

    private static CreateUserRequest buildCreateUserRequest(String username, String password, String email) {
        return CreateUserRequest.builder().username(username).password(password).email(email).build();
    }

    private void createUserApi() throws Exception {
        mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + SIGN_UP)
                        .content(writeValueAsString(buildCreateUserRequest(USERNAME, PASSWORD, EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    private String getAuthToken() throws Exception {
        createUserApi();
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + LOGIN)
                        .content(writeValueAsString(buildLoginUserRequest(USERNAME, PASSWORD)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        ResponsePayload<LoginResponse> responsePayload = new ObjectMapper()
                .readValue(actualResponse, new TypeReference<>() {
                });
        return responsePayload.getResult().getToken();
    }

    private static LoginUserRequest buildLoginUserRequest(String username, String password) {
        return LoginUserRequest.builder().username(username).password(password).build();
    }

    @Test
    void createUser_Success() throws Exception {
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + SIGN_UP)
                        .content(writeValueAsString(buildCreateUserRequest(USERNAME, PASSWORD, EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve("user").resolve("createUser_success.json"));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    static Stream<Arguments> test_createUser_requestPayload_error() {
        Path path = basePath.resolve("user");
        return Stream.of(
                Arguments.of("Test createUser where mandatory fields is not provided", buildCreateUserRequest(
                        null, null, null), path.resolve("createUser_missingMandatory_error.json")),
                Arguments.of("Test createUser with invalid email format", buildCreateUserRequest(USERNAME,
                        PASSWORD, "sample.email"), path.resolve("createUser_invalidEmail_error.json"))
        );
    }

    @ParameterizedTest
    @MethodSource("test_createUser_requestPayload_error")
    void createUser_requestPayload_Failure(String name, CreateUserRequest request, Path expectedFile) throws Exception {
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + SIGN_UP)
                        .content(writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(expectedFile);
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    static Stream<Arguments> test_createUser_existingUser_error() {
        Path path = basePath.resolve("user");
        return Stream.of(
                Arguments.of("Test createUser where there is already an existing username", buildUserDetails(
                        USERNAME, "sample@test.com"), path.resolve("createUser_existingUsername_error.json")),
                Arguments.of("Test createUser where there the email is used", buildUserDetails(
                        "test", EMAIL), path.resolve("createUser_existingEmail_error.json"))
        );
    }

    @ParameterizedTest
    @MethodSource("test_createUser_existingUser_error")
    void createUser_existingUser_Failure(String name, User savedUser, Path expectedFile) throws Exception {
        userRepository.save(savedUser);
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + SIGN_UP)
                        .content(writeValueAsString(buildCreateUserRequest(USERNAME, PASSWORD, EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(expectedFile);
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    void userLogin_Success() throws Exception {
        createUserApi();
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + LOGIN)
                        .content(writeValueAsString(buildLoginUserRequest(USERNAME, PASSWORD)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        ResponsePayload<LoginResponse> responsePayload = new ObjectMapper()
                .readValue(actualResponse, new TypeReference<>() {
                });

        String expectedResponse = Files.readString(basePath.resolve("login").resolve("loginUser_success.json"));
        expectedResponse = expectedResponse.replace("#token#", responsePayload.getResult().getToken());
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    void userLogin_missingMandatory_Failure() throws Exception {
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + LOGIN)
                        .content(writeValueAsString(buildLoginUserRequest(null, null)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve("login").resolve(
                "loginUser_missingMandatory_error.json"));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    void userLogin_userNotFound_Failure() throws Exception {
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + OPEN + API_VERSION_1 + LOGIN)
                        .content(writeValueAsString(buildLoginUserRequest(USERNAME, PASSWORD)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve("login").resolve(
                "loginUser_userNotFound_error.json"));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    void refreshToken_Success() throws Exception {
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + SECURE + API_VERSION_1 + REFRESH)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + getAuthToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        ResponsePayload<LoginResponse> responsePayload = new ObjectMapper()
                .readValue(actualResponse, new TypeReference<>() {
                });

        String expectedResponse = Files.readString(basePath.resolve("refresh_token")
                .resolve("refreshToken_success.json"));
        expectedResponse = expectedResponse.replace("#token#", responsePayload.getResult().getToken());
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    void refreshToken_Failure() throws Exception {
        String actualResponse = mvc.perform(post(API_AUTHENTICATION + SECURE + API_VERSION_1 + REFRESH)
                        .header(HttpHeaders.AUTHORIZATION, BEARER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve("refresh_token")
                .resolve("refreshToken_error.json"));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }
}
