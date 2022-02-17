package com.github.amirbaratpoor.lucene;

import org.locationtech.jts.geom.*;

import java.util.List;

public record TestModel(String id, String description, Geometry geometry) {
    private static final List<TestModel> SOURCE_DATA = sourceData();

    private static List<TestModel> sourceData() {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        TestModel center = new TestModel("point", "Earth's center", geometryFactory.createPoint(new Coordinate(0, 0)));
        TestModel circle = new TestModel("polygon", "Random circle around earth's center", center.geometry.buffer(0.1));
        LineString lineString = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(-1, 0),
                new Coordinate(-1, -0.5),
                new Coordinate(-0.5, -0.5),
                new Coordinate(-0.5, -1)
        });
        TestModel line = new TestModel("line", "Random LineString", lineString);
        TestModel polygon = new TestModel("polygon", "Buffer around LineString", lineString.buffer(0.1));
        return List.of(center, circle, line, polygon);
    }

    public static List<TestModel> getSourceData() {
        return SOURCE_DATA;
    }
}
