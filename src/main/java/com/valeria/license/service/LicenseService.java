package com.valeria.license.service;

import com.valeria.entity.AppUser;
import com.valeria.license.dto.ActivateLicenseRequest;
import com.valeria.license.dto.CreateLicenseRequest;
import com.valeria.license.dto.LicenseResponse;
import com.valeria.license.entity.*;
import com.valeria.license.repository.*;
import com.valeria.repository.UserRepository;
import com.valeria.license.security.TicketSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.valeria.license.ticket.Ticket;
import com.valeria.license.ticket.TicketResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final TicketSigner ticketSigner;

                        // ---CREATE---
    // Создание лицензии администратором
    @Transactional
    public LicenseResponse createLicense(CreateLicenseRequest request, UUID adminId) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("продукт не найден"));

        LicenseType type = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new RuntimeException("тип лицензии не найден"));

        AppUser owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("владелец не найден"));

        // Генерация activation key
        String code = UUID.randomUUID().toString();

        // Создание сущности лицензии
        License license = License.builder()
                .code(code)
                .product(product)
                .type(type)
                .owner(owner)
                .user(null)
                .deviceCount(request.getDeviceCount())
                .blocked(false)
                .description(request.getDescription())
                .build();

        licenseRepository.save(license);

        // Сохранение истории (создание лицензии)
        AppUser admin = userRepository.findById(adminId).orElseThrow();

        LicenseHistory history = LicenseHistory.builder()
                .license(license)
                .user(admin)
                .status("CREATED")
                .changeDate(LocalDate.now())
                .description("создание лицензии")
                .build();

        historyRepository.save(history);

        return LicenseResponse.builder()
                .id(license.getId())
                .code(license.getCode())
                .productId(license.getProduct().getId())
                .typeId(license.getType().getId())
                .deviceCount(license.getDeviceCount())
                .blocked(license.getBlocked())
                .build();
    }
                        // ---ACTIVATE---
    // Активация лицензии пользователем на устройстве
    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, UUID userId) {

        // Поиск лицензии по activation key
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new RuntimeException("лицензия не найдена"));

        AppUser user = userRepository.findById(userId).orElseThrow();

        // Проверка блокировки лицензии
        if (Boolean.TRUE.equals(license.getBlocked())) {
            throw new RuntimeException("лицензия заблокирована");
        }

        // Проверка владельца (нельзя активировать чужую лицензию)
        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new RuntimeException("лицензия принадлежит другому пользователю");
        }

        // Проверка срока действия
        if (license.getEndingDate() != null &&
                license.getEndingDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("срок действия лицензии истёк");
        }

        // Поиск или создание устройства
        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElse(null);

        if (device == null) {
            device = Device.builder()
                    .name(request.getDeviceName())
                    .macAddress(request.getDeviceMac())
                    .user(user)
                    .build();

            deviceRepository.save(device);
        }

        // Проверка: устройство уже активировано
        boolean exists = deviceLicenseRepository
                .existsByLicenseIdAndDeviceId(license.getId(), device.getId());

        if (exists) {
            throw new RuntimeException("устройство уже активировано");
        }

                        // ---ПЕРВАЯ АКТИВАЦИЯ---
        if (license.getUser() == null) {

            license.setUser(user);
            license.setFirstActivationDate(LocalDate.now());

            // Расчёт срока действия
            LocalDate endingDate = LocalDate.now()
                    .plusDays(license.getType().getDefaultDurationInDays());

            license.setEndingDate(endingDate);

            licenseRepository.save(license);

            // Связь лицензии и устройства
            DeviceLicense deviceLicense = DeviceLicense.builder()
                    .license(license)
                    .device(device)
                    .activationDate(LocalDate.now())
                    .build();

            deviceLicenseRepository.save(deviceLicense);

            // История
            historyRepository.save(buildHistory(license, user, "ACTIVATED", "первая активация"));

            return buildTicket(license, device);
        }

                        // ---ПОВТОРНАЯ АКТИВАЦИЯ---
        long count = deviceLicenseRepository.countByLicenseId(license.getId());

        // Проверка лимита устройств
        if (count >= license.getDeviceCount()) {
            throw new RuntimeException("достигнут лимит устройств");
        }

        deviceLicenseRepository.save(DeviceLicense.builder()
                .license(license)
                .device(device)
                .activationDate(LocalDate.now())
                .build());

        historyRepository.save(buildHistory(license, user, "ACTIVATED", "повторная активация"));

        return buildTicket(license, device);
    }

                        // ---CHECK---
    // Проверка лицензии для устройства и продукта
    public TicketResponse checkLicense(String deviceMac, UUID userId, UUID productId) {

        Device device = deviceRepository.findByMacAddress(deviceMac)
                .orElseThrow(() -> new RuntimeException("устройство не найдено"));

        License license = licenseRepository
                .findActive(device.getId(), userId, productId)
                .orElseThrow(() -> new RuntimeException("лицензия не найдена"));

        // Проверка блокировки
        if (Boolean.TRUE.equals(license.getBlocked())) {
            throw new RuntimeException("лицензия заблокирована");
        }

        // Проверка владельца
        if (!license.getUser().getId().equals(userId)) {
            throw new RuntimeException("это не ваша лицензия");
        }

        return buildTicket(license, device);
    }

                        // ---RENEW---
    // Продление лицензии по activation key
    @Transactional
    public TicketResponse renewLicense(String activationKey, UUID userId) {

        License license = licenseRepository.findByCode(activationKey)
                .orElseThrow(() -> new RuntimeException("лицензия не найдена"));

        // Проверка блокировки
        if (Boolean.TRUE.equals(license.getBlocked())) {
            throw new RuntimeException("лицензия заблокирована");
        }

        // Проверка владельца
        if (!license.getUser().getId().equals(userId)) {
            throw new RuntimeException("это не ваша лицензия");
        }

        LocalDate today = LocalDate.now();
        LocalDate ending = license.getEndingDate();

        // Если истекла — считаем от текущей даты
        if (ending == null || ending.isBefore(today)) {
            ending = today;
        }

        // Продление срока
        LocalDate newEndingDate = ending
                .plusDays(license.getType().getDefaultDurationInDays());

        license.setEndingDate(newEndingDate);
        licenseRepository.save(license);

        AppUser user = userRepository.findById(userId).orElseThrow();

        historyRepository.save(buildHistory(license, user, "RENEWED", "продление"));

        return buildTicket(license, null);
    }
                        // ---HELPERS---
    // Создание записи истории
    private LicenseHistory buildHistory(License license, AppUser user, String status, String description) {
        return LicenseHistory.builder()
                .license(license)
                .user(user)
                .status(status)
                .changeDate(LocalDate.now())
                .description(description)
                .build();
    }

    // Формирование ticket + подпись
    private TicketResponse buildTicket(License license, Device device) {

        Ticket ticket = new Ticket(
                Instant.now(),
                300,
                license.getFirstActivationDate(),
                license.getEndingDate(),
                license.getUser().getId(),
                device != null ? device.getId() : null,
                license.getBlocked()
        );

        String sign = ticketSigner.signTicket(ticket);

        return new TicketResponse(ticket, sign);
    }
}