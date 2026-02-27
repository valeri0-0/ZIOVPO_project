package com.valeria.controller;

import com.valeria.entity.AppUser;
import com.valeria.model.RegisterDto;
import com.valeria.model.enums.ApplicationUserRole;
import com.valeria.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {

        // Проверка существования username
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Имя пользователя уже занято!");
        }

        // Проверка существования email
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email уже используется!");
        }

        // Запрет регистрации ADMIN
        if (registerDto.getRole() != null &&
                registerDto.getRole().equalsIgnoreCase("ADMIN")) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Регистрация с ролью ADMIN запрещена!");
        }

        // Создание пользователя
        AppUser user = AppUser.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .role(ApplicationUserRole.USER)
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Пользователь успешно зарегистрирован!");
    }
}