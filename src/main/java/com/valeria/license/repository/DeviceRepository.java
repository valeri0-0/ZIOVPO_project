package com.valeria.license.repository;

import com.valeria.license.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// Репозиторий устройств пользователя
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    // Поиск устройства по MAC-адресу
    Optional<Device> findByMacAddress(String macAddress);
}