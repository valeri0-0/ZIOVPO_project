package com.valeria.controller;

import com.valeria.entity.AppUser;
import com.valeria.model.RegisterDto;
import com.valeria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterDto registerDto) {

        // проверка имени
        if (userRepository.findByName(registerDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Имя пользователя уже занято!");
        }

        // проверка email
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email уже используется!");
        }

        AppUser user = AppUser.builder()
                .name(registerDto.getUsername())
                .email(registerDto.getEmail())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .role("USER")
                .isAccountExpired(false)
                .isAccountLocked(false)
                .isCredentialsExpired(false)
                .isDisabled(false)
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Пользователь успешно зарегистрирован!");
    }
}
