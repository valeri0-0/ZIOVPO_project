package com.valeria.license.repository;

import com.valeria.license.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// Репозиторий истории лицензий
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}