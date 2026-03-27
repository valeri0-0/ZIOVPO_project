package com.valeria.signature;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SigningService {

    private final JsonCanonicalizer canonicalizer;
    private final SignatureKeyStoreService keyStoreService;

    public String sign(Object payload) {

        try {
            // 1. Приводим payload к каноническому JSON (RFC 8785)
            String canonicalJson = canonicalizer.canonizeJson(payload);

            // 2. Переводим в UTF-8 байты
            byte[] data = canonicalJson.getBytes(StandardCharsets.UTF_8);

            // 3. Получаем приватный ключ из keystore
            PrivateKey privateKey = keyStoreService.getPrivateKey();

            // 4. Создаем объект подписи
            Signature signature = Signature.getInstance("SHA256withRSA");

            // 5. Инициализируем подпись приватным ключом
            signature.initSign(privateKey);

            // 6. Передаем данные
            signature.update(data);

            // 7. Получаем подпись
            byte[] signedBytes = signature.sign();

            // 8. Кодируем в Base64
            return Base64.getEncoder().encodeToString(signedBytes);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании ЭЦП", e);
        }
    }
}