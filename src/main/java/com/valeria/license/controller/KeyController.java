package com.valeria.license.controller;

import com.valeria.signature.SignatureKeyStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

// "Контроллер для получения публичного ключа"
@RestController
@RequiredArgsConstructor
public class KeyController {

    private final SignatureKeyStoreService keyStoreService;

    @GetMapping("/public-key")
    public String getPublicKey() {

        // "возвращаем публичный ключ из keystore"
        return Base64.getEncoder()
                .encodeToString(keyStoreService.getPublicKey().getEncoded());
    }
}