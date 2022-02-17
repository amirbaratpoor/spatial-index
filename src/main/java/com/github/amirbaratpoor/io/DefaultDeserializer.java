package com.github.amirbaratpoor.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

public class DefaultDeserializer<T> implements Deserializer<T> {

    @SuppressWarnings("rawtypes")
    private static final Deserializer INSTANCE = new DefaultDeserializer<>();

    private DefaultDeserializer() {
    }

    @SuppressWarnings("unchecked")
    public static <T> DefaultDeserializer<T> getInstance() {
        return (DefaultDeserializer<T>) INSTANCE;
    }

    @Override
    public T deserialize(byte[] bytes) throws IOException {
        try (ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(bytes);
             ObjectInputStream objectIS = new ObjectInputStream(byteArrayIS)) {
            try {
                //noinspection unchecked
                return (T) objectIS.readObject();
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Can not deserialize " + Arrays.toString(bytes));
            }
        }
    }
}
