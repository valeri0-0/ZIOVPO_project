package com.valeria.binary;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class MultipartMixedResponseFactory {

    // формирует multipart/mixed ответ
    public ResponseEntity<MultiValueMap<String, Object>> create(byte[] manifestBytes, byte[] dataBytes) {

        // контейнер для нескольких частей ответа
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("manifest", createPart("manifest.bin", manifestBytes));
        body.add("data", createPart("data.bin", dataBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/mixed"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    // создаёт одну часть multipart
    private HttpEntity<ByteArrayResource> createPart(String filename, byte[] content) {

        HttpHeaders partHeaders = new HttpHeaders();

        partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM); // бинарные данные
        partHeaders.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build() // имя файла
        );

        // оборачиваем byte[] как ресурс
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        return new HttpEntity<>(resource, partHeaders);
    }
}