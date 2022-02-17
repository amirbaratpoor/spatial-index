package com.github.amirbaratpoor.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class DefaultSerializer<T> implements Serializer<T> {
    @SuppressWarnings("rawtypes")
    private static final DefaultSerializer INSTANCE = new DefaultSerializer<>();

    private DefaultSerializer() {
    }

    @SuppressWarnings("unchecked")
    public static <T> DefaultSerializer<T> getInstance() {
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
