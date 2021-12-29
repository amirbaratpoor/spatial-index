package com.github.amirbaratpoor.io;

import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;

public final class JacksonDeserializer<T> implements Deserializer<T> {

    private final ObjectReader objectReader;

    public JacksonDeserializer(ObjectReader objectReader) {
        this.objectReader = objectReader;
    }

    @Override
    public T deserialize(byte[] bytes) throws IOException {
        return objectReader.readValue(bytes);
    }
}
