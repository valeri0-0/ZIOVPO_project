package com.valeria.signature;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
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

    public boolean verify(Object payload, String signatureBase64) {

        try {
            // 1. канонизируем payload
            String canonicalJson = canonicalizer.canonizeJson(payload);
            byte[] data = canonicalJson.getBytes(StandardCharsets.UTF_8);

            // 2. декодируем подпись
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            // 3. получаем публичный ключ
            PublicKey publicKey = keyStoreService.getPublicKey();

            // 4. создаём verifier
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);

            // 5. передаём данные
            signature.update(data);

            // 6. проверка
            return signature.verify(signatureBytes);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке подписи", e);
        }
    }

    // подпись готовых байт (для manifest.bin)
    public byte[] signBytes(byte[] data) {
        try {
            PrivateKey privateKey = keyStoreService.getPrivateKey();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);

            return signature.sign();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при подписании byte[]", e);
        }
    }
}