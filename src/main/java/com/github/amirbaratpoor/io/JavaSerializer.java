package com.github.amirbaratpoor.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JavaSerializer<T> implements Serializer<T> {
    @SuppressWarnings("rawtypes")
    private static final JavaSerializer INSTANCE = new JavaSerializer<>();

    private JavaSerializer() {
    }

    @SuppressWarnings("unchecked")
    public static <T> JavaSerializer<T> getInstance() {
        return INSTANCE;
    }

    @Override
    public byte[] serialize(T source) throws IOException {
        try (ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
             ObjectOutputStream objectOS = new ObjectOutputStream(byteArrayOS)) {
            objectOS.writeObject(source);
            objectOS.flush();
            return byteArrayOS.toByteArray();
        }
    }
}
