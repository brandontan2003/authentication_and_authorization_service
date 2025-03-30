package com.example.auth.service.controller;

import com.example.auth.service.model.User;
import com.example.auth.service.repository.UserRepository;
import com.example.auth.service.service.JwtService;
import com.example.auth.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.example.auth.service.TestUtils.*;
import static com.example.auth.service.TestUtils.TestUser.*;
import static com.example.auth.service.common.constant.ApiConstant.BEARER;
import static com.example.auth.service.constant.UriConstant.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserController userController;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void teardown() {
        userRepository.deleteAll();
    }

    private static final Path basePath = Paths.get("src", "test", "resources", "expected_output", "user");

    private String generateToken() {
        return jwtService.generateToken(buildUserDetails());
    }

    private User buildUserDetails() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        return user;
    }

    @Test
    void retrieveUserProfile_Success() throws Exception {
        userRepository.save(buildUserDetails());
        String actualResponse = mvc.perform(get(API_USER + API_VERSION_1 + PROFILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + generateToken()))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve("retrieveUserProfile_success.json"));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    void retrieveUserProfile_noAuthorization_Failure() throws Exception {
        String actualResponse = mvc.perform(get(API_USER + API_VERSION_1 + PROFILE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve("retrieveUserProfile_noAuth_error.json"));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }

    static Stream<Arguments> test_retrieveUserProfile_invalidAuthorization_Failure() {
        return Stream.of(
                Arguments.of("Test retrieveUserProfile where token is not provided", BEARER,
                        "retrieveUserProfile_noToken_error.json"),
                Arguments.of("Test retrieveUserProfile with invalid token", BEARER + "test",
                        "retrieveUserProfile_invalidToken_error.json")
        );
    }

    @ParameterizedTest
    @MethodSource("test_retrieveUserProfile_invalidAuthorization_Failure")
    void retrieveUserProfile_invalidAuthorization_Failure(String name, String authorization, Path expectedFile) throws Exception {
        String actualResponse = mvc.perform(get(API_USER + API_VERSION_1 + PROFILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();
        log.info(ACTUAL_RESPONSE + writeValueAsString(actualResponse));

        String expectedResponse = Files.readString(basePath.resolve(expectedFile));
        log.info(EXPECTED_RESPONSE + expectedResponse);

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
    }
}
