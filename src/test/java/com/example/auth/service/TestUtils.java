package com.example.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TestUtils {

    public static final String ACTUAL_RESPONSE = "Actual Response: ";
    public static final String EXPECTED_RESPONSE = "Expected Response: ";

    public static <T> String writeValueAsString(T value) throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(value);
    }

    public static class TestUser {
        public static final String USERNAME = "username";
        public static final String EMAIL = "email@test.com";
        public static final String PASSWORD = "1234";
    }

}
