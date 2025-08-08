package com.example.bankcards.controller;


import com.example.bankcards.dto.JwtAuthResponseDto;
import com.example.bankcards.dto.LoginDto;
import com.example.bankcards.dto.RegisterDto;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@Tag(name = "Контроллер Аутентификации", description = "Операции для регистрации и входа в систему")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @ApiResponse(responseCode = "200", description = "Успешный вход, токен в теле ответа")
    @ApiResponse(responseCode = "403", description = "Ошибка входа. Неверные учетные данные")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthResponseDto(token));
    }

    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя с ролью USER")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации или имя пользователя уже занято")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        String result = userService.registerUser(registerDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

}
