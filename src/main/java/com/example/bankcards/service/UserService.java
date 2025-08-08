package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterDto;


public interface UserService {
    String registerUser(RegisterDto registerDto);
}
