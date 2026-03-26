package com.valeria.license.entity;

import com.valeria.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

// Сущность лицензии программного продукта
@Entity
@Table(name = "license")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private LicenseType type;

    @Column(name = "first_activation_date")
    private LocalDate firstActivationDate;

    @Column(name = "ending_date")
    private LocalDate endingDate;

    private Boolean blocked;

    @Column(name = "device_count")
    private Integer deviceCount;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    private String description;
}