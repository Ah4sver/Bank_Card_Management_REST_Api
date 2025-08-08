package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.LoginDto;
import com.example.bankcards.dto.RegisterDto;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void registerUser_WithValidData_ShouldReturnCreated() throws Exception {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("testuser_password");
        registerDto.setFirstName("Ivan");
        registerDto.setLastName("Ivanov");

        given(userService.registerUser(any(RegisterDto.class)))
                .willReturn("Пользователь успешно зарегистрирован!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
//                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andExpect(content().string("Пользователь успешно зарегистрирован!"));
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnToken() throws Exception {

        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("testuser_password");

        Authentication authentication = new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtTokenProvider.generateToken(any(Authentication.class))).willReturn("BestJwtToken");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("BestJwtToken"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
