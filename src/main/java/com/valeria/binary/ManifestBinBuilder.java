package com.valeria.binary;

import com.valeria.signature.SigningService;
import com.valeria.signature.entity.MalwareSignature;
import com.valeria.signature.model.SignatureStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManifestBinBuilder {

    private final SigningService signingService;

    // формирует manifest.bin и подписывает его
    public byte[] build(
            List<MalwareSignature> signatures,
            byte[] dataBytes,
            Instant since,
            ExportType type
    ) {

        BinaryWriter writer = new BinaryWriter();

        // HEADER
        writer.writeString("MF-Bushueva");
        writer.writeU16(1);
        writer.writeU8(mapExportType(type));
        writer.writeI64(Instant.now().toEpochMilli()); // время формирования манифеста

        writer.writeI64(since == null ? -1 : since.toEpochMilli()); // время since (для инкремента)

        writer.writeU32(signatures.size()); // сколько записей в манифесте

        // HASH DATA
        // считаем SHA-256 от data.bin
        byte[] hash = calculateSha256(dataBytes);

        // проверка, что хэш ровно 32 байта
        if (hash.length != 32) {
            throw new IllegalStateException("SHA-256 должен быть 32 байта");
        }

        // записываем хэш data.bin
        writer.writeBytes(hash);

        // ENTRIES

        long offset = 0; // текущее смещение в data.bin

        for (MalwareSignature s : signatures) {

            // проверка корректности диапазона
            if (s.getOffsetEnd() < s.getOffsetStart()) {
                throw new IllegalStateException("offsetEnd не может быть меньше offsetStart");
            }

            // id сигнатуры
            writer.writeUUID(s.getId());

            // статус (ACTUAL / DELETED -> число)
            writer.writeU8(mapStatus(s.getStatus()));

            // время последнего обновления
            writer.writeI64(s.getUpdatedAt().toEpochMilli());

            // длина записи в data.bin
            int length = calculateRecordLength(s);

            // смещение записи в data.bin
            writer.writeU32(offset);

            // длина записи
            writer.writeU32(length);

            // подпись записи (берем из БД и декодируем из Base64)
            byte[] recordSignature = Base64.getDecoder()
                    .decode(s.getDigitalSignatureBase64());

            // записываем длину подписи
            writer.writeU32(recordSignature.length);

            // записываем саму подпись
            writer.writeBytes(recordSignature);

            // увеличиваем смещение для следующей записи
            offset += length;
        }

        // ЭЦП МАНИФЕСТА

        // получаем байты манифеста без подписи
        byte[] withoutSignature = writer.toByteArray();

        // подписываем весь манифест
        byte[] signature = signingService.signBytes(withoutSignature);

        BinaryWriter finalWriter = new BinaryWriter();

        // сначала записываем сам манифест
        finalWriter.writeBytes(withoutSignature);

        // затем длину подписи
        finalWriter.writeU32(signature.length);

        // затем саму подпись
        finalWriter.writeBytes(signature);

        // возвращаем готовый manifest.bin
        return finalWriter.toByteArray();
    }

    // перевод типа выгрузки в число
    private int mapExportType(ExportType type) {
        return switch (type) {
            case FULL -> 1;
            case INCREMENT -> 2;
            case BY_IDS -> 3;
        };
    }

    // перевод статуса в число
    private int mapStatus(SignatureStatus status) {
        return status == SignatureStatus.ACTUAL ? 1 : 2;
    }

    // считаем SHA-256 от data.bin
    private byte[] calculateSha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // считаем длину записи в data.bin
    private int calculateRecordLength(MalwareSignature s) {

        int length = 0;

        // строка = 4 байта длины + сами байты
        length += 4 + s.getThreatName().getBytes(StandardCharsets.UTF_8).length;

        // HEX -> byte[] (длина / 2) + 4 байта длины
        length += 4 + (s.getFirstBytesHex().length() / 2);

        length += 4 + (s.getRemainderHashHex().length() / 2);

        // long = 8 байт
        length += 8;

        // строка fileType
        length += 4 + s.getFileType().getBytes(StandardCharsets.UTF_8).length;

        // offsetStart + offsetEnd (по 8 байт)
        length += 8 + 8;

        return length;
    }
}