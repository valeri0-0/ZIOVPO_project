package com.valeria.license.entity;

import com.valeria.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

// Сущность истории изменений состояния лицензии
@Entity
@Table(name = "license_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String status;

    @Column(name = "change_date")
    private LocalDate changeDate;

    private String description;
}