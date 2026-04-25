package com.valeria.license.controller;

import com.valeria.entity.AppUser;
import com.valeria.license.dto.*;
import com.valeria.license.service.LicenseService;
import com.valeria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// Контроллер управления лицензиями
@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final UserRepository userRepository;

    // Получение текущего пользователя из SecurityContext
    private AppUser getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("пользователь не найден"));
    }

    // Создание лицензии (admin)
    @PostMapping
    public ResponseEntity<LicenseResponse> createLicense(@RequestBody CreateLicenseRequest request) {

        AppUser user = getCurrentUser();

        return ResponseEntity.status(201).body(
                licenseService.createLicense(request, user.getId())
        );
    }

    // Активация лицензии
    @PostMapping("/activate")
    public ResponseEntity<?> activateLicense(@RequestBody ActivateLicenseRequest request) {

        AppUser user = getCurrentUser();

        try {
            return ResponseEntity.ok(
                    licenseService.activateLicense(request, user.getId())
            );
        } catch (RuntimeException e) {

            if (e.getMessage().equals("лицензия не найдена"))
                return ResponseEntity.status(404).body(e.getMessage());

            if (e.getMessage().equals("лицензия принадлежит другому пользователю"))
                return ResponseEntity.status(403).body(e.getMessage());

            if (e.getMessage().equals("достигнут лимит устройств"))
                return ResponseEntity.status(409).body(e.getMessage());

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Проверка лицензии
    @PostMapping("/check")
    public ResponseEntity<?> checkLicense(@RequestBody CheckLicenseRequest request) {

        AppUser user = getCurrentUser();

        return ResponseEntity.ok(
                licenseService.checkLicense(
                        request.getDeviceMac(),
                        user.getId(),
                        request.getProductId()
                )
        );
    }

    // Продление лицензии
    @PostMapping("/renew")
    public ResponseEntity<?> renewLicense(@RequestBody RenewLicenseRequest request) {

        AppUser user = getCurrentUser();

        try {
            return ResponseEntity.ok(
                    licenseService.renewLicense(
                            request.getActivationKey(),
                            user.getId()
                    )
            );
        } catch (RuntimeException e) {

            if (e.getMessage().equals("лицензия не найдена"))
                return ResponseEntity.status(404).body(e.getMessage());

            if (e.getMessage().equals("продление недоступно"))
                return ResponseEntity.status(409).body(e.getMessage());

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}