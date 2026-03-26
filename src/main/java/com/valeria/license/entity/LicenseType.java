package com.valeria.license.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

// Сущность типа лицензии с параметрами и сроком действия
@Entity
@Table(name = "license_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "default_duration_in_days")
    private Integer defaultDurationInDays;

    private String description;
}