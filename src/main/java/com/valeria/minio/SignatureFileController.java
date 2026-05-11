package com.valeria.minio;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SignatureFileController {

    private final MinioService minioService;

    @PostMapping("/api/signatures/upload")
    public SignatureUploadResponse uploadFile(
            @RequestParam("file")
            MultipartFile file
    ) {

        return minioService.uploadFile(file);
    }

    @PostMapping("/api/signatures/urls")
    public List<SignatureUrlResponse> getUrls(
            @RequestBody List<Long> ids
    ) {

        return minioService.getPresignedUrls(ids);
    }
}