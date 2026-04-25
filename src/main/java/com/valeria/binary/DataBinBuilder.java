package com.valeria.binary;

import com.valeria.signature.entity.MalwareSignature;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataBinBuilder {

    // формирует data.bin (только полезная нагрузка)
    public byte[] build(List<MalwareSignature> signatures) {

        BinaryWriter writer = new BinaryWriter();

        // header
        writer.writeString("DB-Bushueva");
        writer.writeU16(1);
        writer.writeU32(signatures.size()); // сколько записей внутри файла

        // записи
        for (MalwareSignature s : signatures) {

            // проверка корректности смещений
            if (s.getOffsetEnd() < s.getOffsetStart()) {
                throw new IllegalStateException("offsetEnd < offsetStart");
            }

            // записываем название сигнатуры: сначала длину строки, потом её байты
            writer.writeString(s.getThreatName());

            // записываем первые байты сигнатуры: сначала длину, потом сами байты (HEX -> byte[])
            writer.writeBytesWithLength(hexToBytes(s.getFirstBytesHex()));

            // записываем хэш сигнатуры: сначала длину, потом байты (HEX -> byte[])
            writer.writeBytesWithLength(hexToBytes(s.getRemainderHashHex()));

            // записываем длину хвоста сигнатуры (фиксированное число, 8 байт)
            writer.writeI64(s.getRemainderLength());

            // записываем тип файла: сначала длину строки, потом её байты
            writer.writeString(s.getFileType());

            // проверка корректности диапазона
            if (s.getOffsetEnd() < s.getOffsetStart()) {
                throw new IllegalStateException("offsetEnd < offsetStart");
            }

            writer.writeI64(s.getOffsetStart());
            writer.writeI64(s.getOffsetEnd());
        }

        return writer.toByteArray();
    }

    // hex -> byte[]
    private byte[] hexToBytes(String hex) {

        // проверка корректности HEX строки
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Некорректная HEX-строка");
        }

        int len = hex.length(); // длина строки
        byte[] result = new byte[len / 2]; // массив байтов

        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }

        return result;
    }
}