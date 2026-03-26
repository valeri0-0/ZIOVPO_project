package com.valeria.controller;

import com.valeria.entity.AppUser;
import com.valeria.entity.UserSession;
import com.valeria.model.AuthRequestDto;
import com.valeria.model.AuthResponseDto;
import com.valeria.model.enums.SessionStatus;
import com.valeria.repository.UserRepository;
import com.valeria.repository.UserSessionRepository;
import com.valeria.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class JwtAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDto request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.warn("Неудачная попытка входа: {}", request.getUsername());
            return ResponseEntity.status(401).body("Неверный логин или пароль");
        }

        // ищем пользователя по email
        AppUser user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_" + user.getRole())
                        .build();

        // создаем сессию
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken("")
                .status(SessionStatus.ACTIVE)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(604800000))
                .build();

        UserSession saved = sessionRepository.save(session);

        // генерируем токены
        String refreshToken = tokenProvider.generateRefreshToken(userDetails, saved.getId());
        String accessToken = tokenProvider.generateAccessToken(userDetails);

        saved.setRefreshToken(refreshToken);
        sessionRepository.save(saved);

        log.info("Успешный вход пользователя: {}", user.getName());

        return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@RequestBody AuthResponseDto request) {

        String oldRefresh = request.getRefreshToken();

        var sessionOpt = sessionRepository.findByRefreshToken(oldRefresh);

        if (sessionOpt.isEmpty()) {
            log.warn("Refresh-токен не найден: {}", maskToken(oldRefresh));
            return ResponseEntity.status(401).body("Refresh-токен недействителен");
        }

        UserSession session = sessionOpt.get();
        SessionStatus status = session.getStatus();

        if (status == SessionStatus.USED) {

            log.warn("Повторное использование refresh-токена! Сессия {}, пользователь {}",
                    session.getId(), session.getUser().getName());

            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(Instant.now());

            sessionRepository.save(session);

            return ResponseEntity.status(401)
                    .body("Повторное использование refresh-токена. Сессия отозвана");
        }

        if (status == SessionStatus.REVOKED) {
            return ResponseEntity.status(401).body("Refresh-токен отозван");
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {

            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(Instant.now());

            sessionRepository.save(session);

            return ResponseEntity.status(403).body("Refresh-токен просрочен");
        }

        session.setStatus(SessionStatus.USED);
        sessionRepository.save(session);

        AppUser user = session.getUser();

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_" + user.getRole())
                        .build();

        UserSession newSession = UserSession.builder()
                .user(user)
                .refreshToken("")
                .status(SessionStatus.ACTIVE)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(604800000))
                .build();

        UserSession savedSession = sessionRepository.save(newSession);

        String newAccess = tokenProvider.generateAccessToken(userDetails);
        String newRefresh = tokenProvider.generateRefreshToken(userDetails, savedSession.getId());

        savedSession.setRefreshToken(newRefresh);
        sessionRepository.save(savedSession);

        log.info("Успешный refresh. Новая сессия: {}, пользователь: {}",
                savedSession.getId(), user.getName());

        return ResponseEntity.ok(new AuthResponseDto(newAccess, newRefresh));
    }

    private String maskToken(String token) {

        if (token == null || token.length() <= 10) {
            return "***";
        }

        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
