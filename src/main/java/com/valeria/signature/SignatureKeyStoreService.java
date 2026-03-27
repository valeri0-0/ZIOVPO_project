package com.valeria.signature;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Service
@RequiredArgsConstructor
public class SignatureKeyStoreService {

    // Конфигурация из application.yml
    private final SignatureProperties properties;

    // Загрузка ресурсов (classpath, file, диск)
    private final ResourceLoader resourceLoader;

    private volatile PrivateKey privateKey;

    private volatile PublicKey publicKey;

        // ---Получить приватный ключ (для подписи)---

    public PrivateKey getPrivateKey() {
        PrivateKey cached = privateKey;

        // Если уже загружен — вернуть из кеша
        if (cached != null) {
            return cached;
        }

        // Потокобезопасная инициализация
        synchronized (this) {
            if (privateKey == null) {
                privateKey = loadPrivateKey();
            }
            return privateKey;
        }
    }


        // ---Получить публичный ключ (для проверки подписи)---

    public PublicKey getPublicKey() {
        PublicKey cached = publicKey;

        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (publicKey == null) {
                publicKey = loadPublicKey();
            }
            return publicKey;
        }
    }

        // ---Загрузка приватного ключа из keystore---

    private PrivateKey loadPrivateKey() {
        KeyStore keyStore = loadKeyStore();

        // alias — имя ключа внутри keystore
        String alias = requireNonBlank(
                properties.getKeyAlias(),
                "Не задан параметр signature.keyAlias"
        );

        char[] keyPassword = resolveKeyPassword();

        try {
            java.security.Key key = keyStore.getKey(alias, keyPassword);

            // Если ключ не найден
            if (key == null) {
                throw new IllegalStateException(
                        "Ключ с alias '" + alias + "' не найден в keystore");
            }

            // Проверка, что это приватный ключ
            if (!(key instanceof PrivateKey privateKeyValue)) {
                throw new IllegalStateException(
                        "Alias '" + alias + "' не содержит приватный ключ");
            }

            return privateKeyValue;

        } catch (Exception ex) {
            throw new IllegalStateException("Ошибка загрузки приватного ключа", ex);
        }
    }

        // ---Загрузка публичного ключа (из сертификата)---

    private PublicKey loadPublicKey() {
        KeyStore keyStore = loadKeyStore();

        String alias = requireNonBlank(
                properties.getKeyAlias(),
                "Не задан параметр signature.keyAlias"
        );

        try {
            Certificate certificate = keyStore.getCertificate(alias);

            if (certificate == null) {
                throw new IllegalStateException(
                        "Сертификат для alias '" + alias + "' не найден");
            }

            // Из сертификата извлекаем публичный ключ
            return certificate.getPublicKey();

        } catch (Exception ex) {
            throw new IllegalStateException("Ошибка загрузки публичного ключа", ex);
        }
    }

        // ---Загрузка keystore (файла с ключами)---

    private KeyStore loadKeyStore() {

        // Путь к keystore обязателен
        String keyStorePath = requireNonBlank(
                properties.getKeyStorePath(),
                "Не задан параметр signature.keyStorePath"
        );

        // Тип keystore (по умолчанию JKS)
        String keyStoreType = properties.getKeyStoreType() == null
                || properties.getKeyStoreType().isBlank()
                ? "JKS"
                : properties.getKeyStoreType();

        // Пароль keystore обязателен
        char[] keyStorePassword = requireNonBlank(
                properties.getKeyStorePassword(),
                "Не задан параметр signature.keyStorePassword"
        ).toCharArray();

        try (InputStream inputStream = openKeyStoreStream(keyStorePath)) {

            KeyStore keyStore = KeyStore.getInstance(keyStoreType);

            // Загружаем keystore
            keyStore.load(inputStream, keyStorePassword);

            return keyStore;

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Ошибка загрузки keystore по пути: " + keyStorePath, ex);
        }
    }

        //---Определение пароля ключа---

    private char[] resolveKeyPassword() {
        String keyPassword = properties.getKeyPassword();

        // Если задан отдельный пароль ключа — используем его
        if (keyPassword != null && !keyPassword.isBlank()) {
            return keyPassword.toCharArray();
        }

        // Иначе используем пароль keystore
        return requireNonBlank(
                properties.getKeyStorePassword(),
                "Не задан параметр signature.keyStorePassword"
        ).toCharArray();
    }

        // ---Открытие потока для keystore---

    private InputStream openKeyStoreStream(String keyStorePath) throws Exception {

        String normalizedPath = keyStorePath.trim();
        String lowerPath = normalizedPath.toLowerCase();

        // Поддержка classpath: и file:
        if (lowerPath.startsWith("classpath:") || lowerPath.startsWith("file:")) {

            Resource resource = resourceLoader.getResource(normalizedPath);

            if (!resource.exists()) {
                throw new IllegalStateException(
                        "Файл keystore не найден: " + normalizedPath);
            }

            return resource.getInputStream();
        }

        // Иначе считаем, что это обычный путь
        return Files.newInputStream(Path.of(normalizedPath));
    }

        // ---Проверка, что значение не пустое---

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }
}