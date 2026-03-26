package com.valeria.license.repository;

import com.valeria.license.entity.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// Репозиторий используется для получения длительности лицензии
public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
}