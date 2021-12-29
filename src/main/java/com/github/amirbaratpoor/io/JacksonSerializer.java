package com.github.amirbaratpoor.io;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.io.IOException;

public class JacksonSerializer<T> implements Serializer<T> {
    private final ObjectWriter objectWriter;

    public JacksonSerializer(ObjectWriter objectWriter) {
        this.objectWriter = objectWriter;
    }

    @Override
    public byte[] serialize(T source) throws IOException {
        return objectWriter.writeValueAsBytes(source);
    }
}
