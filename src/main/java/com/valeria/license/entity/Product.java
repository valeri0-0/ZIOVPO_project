package com.valeria.license.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

// Сущность программного продукта, для которого создаются лицензии
@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "is_blocked")
    private Boolean isBlocked;
}