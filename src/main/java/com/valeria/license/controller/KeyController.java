package com.valeria.license.controller;

import com.valeria.license.crypto.KeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
public class KeyController {

    private final KeyProvider keyProvider;

    @GetMapping("/public-key")
    public String getPublicKey() {

        return Base64.getEncoder()
                .encodeToString(keyProvider.getPublicKey().getEncoded());
    }
}