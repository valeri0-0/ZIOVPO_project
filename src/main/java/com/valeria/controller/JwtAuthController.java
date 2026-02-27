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

        // Загружаем пользователя
        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().getGrantedAuthorities())
                .build();

        // Создаем сессию
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken("")
                .status(SessionStatus.ACTIVE)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(604800000)) // 7 дней
                .build();

        UserSession saved = sessionRepository.save(session);

        // Генерируем токены
        String refreshToken = tokenProvider.generateRefreshToken(userDetails, saved.getId());
        String accessToken = tokenProvider.generateAccessToken(userDetails);

        // Обновляем refresh токен
        saved.setRefreshToken(refreshToken);
        sessionRepository.save(saved);

        log.info("Успешный вход пользователя: {}", user.getUsername());
        return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@RequestBody AuthResponseDto request) {
        String oldRefresh = request.getRefreshToken();

        // Ищем сессию по токену
        var sessionOpt = sessionRepository.findByRefreshToken(oldRefresh);

        if (sessionOpt.isEmpty()) {
            log.warn("Refresh-токен не найден: {}", maskToken(oldRefresh));
            return ResponseEntity.status(401).body("Refresh-токен недействителен");
        }

        UserSession session = sessionOpt.get();
        SessionStatus status = session.getStatus();

        log.debug("Найдена сессия ID: {}, статус: {}", session.getId(), status);

        // Сессия уже USED
        if (status == SessionStatus.USED) {
            log.warn("Обнаружено повторное использование refresh-токена! Сессия: {}, Пользователь: {}",
                    session.getId(), session.getUser().getUsername());

            // При повторном использовании меняем статус на REVOKED
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(Instant.now());  // Записываем время отзыва
            sessionRepository.save(session);

            return ResponseEntity.status(401)
                    .body("Обнаружено повторное использование refresh-токена. Сессия отозвана");
        }

        // Сессия уже REVOKED
        if (status == SessionStatus.REVOKED) {
            log.warn("Попытка использования отозванного refresh-токена. Сессия: {}", session.getId());
            return ResponseEntity.status(401).body("Refresh-токен отозван");
        }

        // Сессия ACTIVE, проверяем срок действия
        if (session.getExpiresAt().isBefore(Instant.now())) {
            log.info("Refresh-токен просрочен. Сессия: {}", session.getId());

            // При истечении срока помечаем как REVOKED
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);

            return ResponseEntity.status(403).body("Refresh-токен просрочен");
        }

        // Помечаем старую сессию как USED
        session.setStatus(SessionStatus.USED);
        sessionRepository.save(session);

        AppUser user = session.getUser();

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().getGrantedAuthorities())
                .build();

        // Создаем НОВУЮ сессию
        UserSession newSession = UserSession.builder()
                .user(user)
                .refreshToken("")  // Временный токен
                .status(SessionStatus.ACTIVE)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(604800000)) // 7 дней
                .build();

        UserSession savedSession = sessionRepository.save(newSession);

        // Генерируем новые токены с привязкой к ID новой сессии
        String newAccess = tokenProvider.generateAccessToken(userDetails);
        String newRefresh = tokenProvider.generateRefreshToken(userDetails, savedSession.getId());

        // Обновляем новую сессию реальным refreshToken
        savedSession.setRefreshToken(newRefresh);
        sessionRepository.save(savedSession);

        log.info("Успешный refresh. Новая сессия: {}, Пользователь: {}",
                savedSession.getId(), user.getUsername());

        return ResponseEntity.ok(new AuthResponseDto(newAccess, newRefresh));
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}