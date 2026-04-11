package com.valeria.signature.repository;

import com.valeria.signature.entity.MalwareSignatureAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SignatureAuditRepository extends JpaRepository<MalwareSignatureAudit, Long> {

    // через связь signature.id
    List<MalwareSignatureAudit> findBySignature_IdOrderByChangedAtDesc(UUID signatureId);

}