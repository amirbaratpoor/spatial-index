package com.github.amirbaratpoor.lucene.bkdtree;

import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.geo.Rectangle;
import org.locationtech.jts.geom.*;

import java.util.Map;

public final class GeometryTransformer {
    private GeometryTransformer() {
    }

    public static Rectangle transform(Envelope e) {
        return new Rectangle(e.getMinY(), e.getMaxY(), e.getMinX(), e.getMaxX());
    }

    public static Point transform(org.locationtech.jts.geom.Point jtsPoint) {
        return new Point(jtsPoint.getY(), jtsPoint.getX());
    }

    public static Polygon transform(org.locationtech.jts.geom.Polygon jtsPolygon) {
        int numInteriorRing = jtsPolygon.getNumInteriorRing();
        Map.Entry<double[], double[]> latLons = extractLatLons(jtsPolygon.getCoordinates());
        if (numInteriorRing > 0) {
            Polygon[] holes = new Polygon[numInteriorRing];
            for (int i = 0; i < numInteriorRing; ++i) {
                LinearRing hole = jtsPolygon.getInteriorRingN(i);
                Map.Entry<double[], double[]> holeLatLons = extractLatLons(hole.getCoordinates());
                holes[i] = new Polygon(holeLatLons.getKey(), holeLatLons.getValue());
            }
            return new Polygon(latLons.getKey(), latLons.getValue(), holes);
        }
        return new Polygon(latLons.getKey(), latLons.getValue());
    }

    public static boolean isAtomic(Geometry geometry) {
        return !(geometry instanceof GeometryCollection);
    }

    public static boolean isSimpleCollection(Geometry geometry) {
        return geometry instanceof MultiLineString ||
                geometry instanceof MultiPoint ||
                geometry instanceof MultiPolygon;
    }

    public static Line transform(LineString jtsLine) {
        Map.Entry<double[], double[]> latLons = extractLatLons(jtsLine.getCoordinates());
        return new Line(latLons.getKey(), latLons.getValue());
    }

    private static Map.Entry<double[], double[]> extractLatLons(Coordinate[] coordinates) {
        double[] lats = new double[coordinates.length];
        double[] lons = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            lats[i] = coordinates[i].y;
            lons[i] = coordinates[i].x;
        }
        return Map.entry(lats, lons);
    }

    public static void process(Geometry geometry, LatLonConsumer consumer) {
        if (isAtomic(geometry)) {
            if (geometry instanceof org.locationtech.jts.geom.Point p) {
                consumer.point(transform(p));
            } else if (geometry instanceof org.locationtech.jts.geom.Polygon p) {
                consumer.polygon(transform(p));
            } else if (geometry instanceof LineString ls) {
                consumer.line(transform(ls));
            } else {
                throw new IllegalArgumentException("Unknown Atomic Geometry" + geometry);
            }
            return;
        }
        GeometryCollection gc = (GeometryCollection) geometry;
        for (int i = 0; i < gc.getNumGeometries(); ++i) {
            process(gc.getGeometryN(i), consumer);
        }
    }


}
