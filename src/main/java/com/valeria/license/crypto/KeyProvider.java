package com.valeria.license.crypto;

import org.springframework.stereotype.Component;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class KeyProvider {

    private final KeyPair keyPair;

    public KeyProvider() {
        try {

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            this.keyPair = generator.generateKeyPair();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации криптографических ключей", e);
        }
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}