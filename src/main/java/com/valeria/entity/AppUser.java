package com.valeria.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @JsonIgnore
    @Column(name = "password_hash")
    private String passwordHash;

    private String email;

    private String role;

    private Boolean isAccountExpired;

    private Boolean isAccountLocked;

    private Boolean isCredentialsExpired;

    private Boolean isDisabled;
}