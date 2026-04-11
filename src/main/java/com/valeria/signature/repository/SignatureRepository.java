package com.valeria.signature.repository;

import com.valeria.signature.entity.MalwareSignature;
import com.valeria.signature.model.SignatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SignatureRepository extends JpaRepository<MalwareSignature, UUID> {

    // только ACTUAL (для полной базы)
    List<MalwareSignature> findByStatus(SignatureStatus status);

    // инкремент (updatedAt > since)
    List<MalwareSignature> findByUpdatedAtAfter(Instant since);

    // по списку id
    List<MalwareSignature> findByIdIn(List<UUID> ids);
}