package com.valeria.minio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "signature_files")
@Getter
@Setter
public class SignatureFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String objectName;

    @Column(unique = true, nullable = false, length = 64)
    private String hash;

    private LocalDateTime uploadedAt;
}