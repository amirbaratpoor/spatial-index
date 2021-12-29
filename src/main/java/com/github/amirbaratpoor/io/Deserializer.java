package com.github.amirbaratpoor.io;

import java.io.IOException;

@FunctionalInterface
public interface Deserializer<T> {

    T deserialize(byte[] bytes) throws IOException;

}
