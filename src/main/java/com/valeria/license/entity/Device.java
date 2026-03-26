package com.valeria.license.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.valeria.entity.AppUser;

// Сущность устройства пользователя, на котором активируется лицензия
@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "mac_address", unique = true)
    private String macAddress;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}