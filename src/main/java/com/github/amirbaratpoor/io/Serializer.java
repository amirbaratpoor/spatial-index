package com.github.amirbaratpoor.io;

import java.io.IOException;

@FunctionalInterface
public interface Serializer<T> {

    byte[] serialize(T source) throws IOException;

}
