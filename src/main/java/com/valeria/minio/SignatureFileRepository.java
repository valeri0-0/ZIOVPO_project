package com.valeria.minio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignatureFileRepository
        extends JpaRepository<SignatureFile, Long> {

    Optional<SignatureFile> findByHash(String hash);
}