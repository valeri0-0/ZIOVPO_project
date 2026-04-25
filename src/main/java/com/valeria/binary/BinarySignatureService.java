package com.valeria.binary;

import com.valeria.signature.entity.MalwareSignature;
import com.valeria.signature.model.SignatureStatus;
import com.valeria.signature.service.MalwareSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BinarySignatureService {

    private final MalwareSignatureService signatureService;
    private final DataBinBuilder dataBuilder;
    private final ManifestBinBuilder manifestBuilder;
    private final MultipartMixedResponseFactory responseFactory;

    // full dump (только ACTUAL)
    public ResponseEntity<?> getFull() {

        var signatures = signatureService.getAll()
                .stream()
                .filter(s -> s.getStatus() == SignatureStatus.ACTUAL)
                .toList();

        return buildResponse(signatures, null, ExportType.FULL);
    }

    // инкремент (ACTUAL + DELETED)
    public ResponseEntity<?> getIncrement(Instant since) {

        var signatures = signatureService.getIncrement(since);

        return buildResponse(signatures, since, ExportType.INCREMENT);
    }

    // выборка по id
    public ResponseEntity<?> getByIds(List<UUID> ids) {

        var signatures = signatureService.getByIds(ids);

        return buildResponse(signatures, null, ExportType.BY_IDS);
    }

    // сборка ответа
    private ResponseEntity<?> buildResponse(
            List<MalwareSignature> signatures,
            Instant since,
            ExportType type
    ) {

        byte[] dataBytes = dataBuilder.build(signatures);

        byte[] manifestBytes = manifestBuilder.build(
                signatures,
                dataBytes,
                since,
                type
        );

        return responseFactory.create(manifestBytes, dataBytes);
    }
}