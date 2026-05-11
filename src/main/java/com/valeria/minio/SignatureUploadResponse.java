package com.valeria.minio;

import java.time.LocalDateTime;

public record SignatureUploadResponse(

        Long id,

        String fileName,

        String hash,

        LocalDateTime uploadedAt,

        String message
) {
}