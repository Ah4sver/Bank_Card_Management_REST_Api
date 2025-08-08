package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_WhenUsernameIsAvailable_ShouldSaveUserWithEncodedPasswordAndUserRole() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("testuser_password");
        registerDto.setFirstName("Ivan");
        registerDto.setLastName("Ivanov");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testuser_password")).thenReturn("encodedPassword");

        String resultMessage = userService.registerUser(registerDto);

        assertEquals("Пользователь успешно зарегистрирован!", resultMessage);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("Ivan", savedUser.getFirstName());
        assertEquals("Ivanov", savedUser.getLastName());
        assertTrue(savedUser.getRoles().contains(Role.USER));
        assertEquals(1, savedUser.getRoles().size());
    }

    @Test
    void registerUser_WhenUsernameIsTaken_ShouldThrowRuntimeException() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("testuser_password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registerDto);
        });

        assertEquals("Ошибка: Имя пользователя уже занято!", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
