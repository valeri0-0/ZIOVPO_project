package com.valeria.license.repository;

import com.valeria.license.entity.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// Репозиторий связи "лицензия — устройство"
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {

    // Считает количество устройств, активированных по лицензии
    long countByLicenseId(UUID licenseId);

    // Проверяет, активирована ли лицензия на данном устройстве
    boolean existsByLicenseIdAndDeviceId(UUID licenseId, UUID deviceId);
}