package com.valeria.license.repository;

import com.valeria.license.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

// Репозиторий лицензий
public interface LicenseRepository extends JpaRepository<License, UUID> {

    // Поиск лицензии по activation key
    Optional<License> findByCode(String code);

    // Поиск активной лицензии по устройству, пользователю и продукту
    @Query("""
        select l from License l
        join DeviceLicense dl on dl.license.id = l.id
        where dl.device.id = :deviceId
        and l.user.id = :userId
        and l.product.id = :productId
        and l.blocked = false
        and l.endingDate >= CURRENT_DATE
    """)
    Optional<License> findActive(UUID deviceId, UUID userId, UUID productId);
}