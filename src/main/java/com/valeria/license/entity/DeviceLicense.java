package com.valeria.license.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

// Сущность связи между лицензией и устройством
@Entity
@Table(name = "device_license")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "activation_date")
    private LocalDate activationDate;
 }