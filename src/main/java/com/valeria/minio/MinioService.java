package com.valeria.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    private final MinioProperties properties;

    private final SignatureFileRepository repository;

    public SignatureUploadResponse uploadFile(
            MultipartFile file
    ) {

        try {

            byte[] bytes = file.getBytes();

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            String hash = HexFormat.of()
                    .formatHex(digest.digest(bytes));

            Optional<SignatureFile> existingFile =
                    repository.findByHash(hash);

            if (existingFile.isPresent()) {

                SignatureFile savedFile =
                        existingFile.get();

                return new SignatureUploadResponse(
                        savedFile.getId(),
                        savedFile.getFileName(),
                        savedFile.getHash(),
                        savedFile.getUploadedAt(),
                        "Файл уже существует"
                );
            }

            String objectName =
                    UUID.randomUUID()
                            + "-"
                            + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            SignatureFile signatureFile =
                    new SignatureFile();

            signatureFile.setFileName(
                    file.getOriginalFilename()
            );

            signatureFile.setObjectName(objectName);

            signatureFile.setHash(hash);

            signatureFile.setUploadedAt(
                    LocalDateTime.now()
            );

            SignatureFile savedFile =
                    repository.save(signatureFile);

            return new SignatureUploadResponse(
                    savedFile.getId(),
                    savedFile.getFileName(),
                    savedFile.getHash(),
                    savedFile.getUploadedAt(),
                    "Файл успешно загружен"
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Ошибка при загрузке файла",
                    e
            );
        }
    }

    public List<SignatureUrlResponse> getPresignedUrls(
            List<Long> ids
    ) {

        try {

            List<SignatureFile> files =
                    repository.findAllById(ids);

            return files.stream()
                    .map(file -> {

                        try {

                            String url =
                                    minioClient.getPresignedObjectUrl(
                                            GetPresignedObjectUrlArgs
                                                    .builder()
                                                    .method(Method.GET)
                                                    .bucket(
                                                            properties.getBucketName()
                                                    )
                                                    .object(
                                                            file.getObjectName()
                                                    )
                                                    .expiry(60 * 60)
                                                    .build()
                                    );

                            return new SignatureUrlResponse(
                                    file.getId(),
                                    file.getFileName(),
                                    url
                            );

                        } catch (Exception e) {

                            throw new RuntimeException(
                                    "Ошибка при генерации URL",
                                    e
                            );
                        }
                    })
                    .toList();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Ошибка при получении URL",
                    e
            );
        }
    }
}