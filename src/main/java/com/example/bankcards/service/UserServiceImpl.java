package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public String registerUser(RegisterDto registerDto) {

        if(userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new RuntimeException("Ошибка: Имя пользователя уже занято!");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());

        user.setRoles(Collections.singleton(Role.USER));

        userRepository.save(user);


        return "Пользователь успешно зарегистрирован!";
    }
}
