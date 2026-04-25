package com.valeria.binary;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryWriter {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final DataOutputStream dos = new DataOutputStream(baos);

    // --- uint8 ---
    public void writeU8(int value) {
        try {
            dos.writeByte(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- uint16 ---
    public void writeU16(int value) {
        try {
            dos.writeShort(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- uint32 ---
    public void writeU32(long value) {
        try {
            dos.writeInt((int) value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- int64 ---
    public void writeI64(long value) {
        try {
            dos.writeLong(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- byte[] ---
    public void writeBytes(byte[] bytes) {
        try {
            dos.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- byte[] с длиной ---
    public void writeBytesWithLength(byte[] bytes) {
        writeU32(bytes.length);
        writeBytes(bytes);
    }

    // --- String (UTF-8) ---
    public void writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeBytesWithLength(bytes);
    }

    // --- UUID (16 байт) ---
    public void writeUUID(UUID uuid) {
        writeI64(uuid.getMostSignificantBits());
        writeI64(uuid.getLeastSignificantBits());
    }

    // --- получить результат ---
    public byte[] toByteArray() {
        return baos.toByteArray();
    }
}