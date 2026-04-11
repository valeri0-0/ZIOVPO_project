package com.valeria.signature.repository;

import com.valeria.signature.entity.MalwareSignatureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SignatureHistoryRepository extends JpaRepository<MalwareSignatureHistory, Long> {

    // через связь signature.id
    List<MalwareSignatureHistory> findBySignature_IdOrderByVersionCreatedAtDesc(UUID signatureId);

}