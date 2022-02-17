package com.github.amirbaratpoor.lucene.bkdtree;

import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.index.IndexableField;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class BKDFieldMaker {

    IndexableField[] makeFields(Geometry geometry) {
        boolean atomic = GeometryTransformer.isAtomic(geometry);
        FieldMaker fieldMaker = new FieldMaker(atomic);
        GeometryTransformer.process(geometry, fieldMaker);
        if (atomic) {
            return fieldMaker.pointField != null ? new IndexableField[]{fieldMaker.pointField} : fieldMaker.shapeFields;
        }
        return fieldMaker.nonAtomicFields.toArray(new IndexableField[0]);
    }

    private static class FieldMaker implements LatLonConsumer {

        private final boolean atomic;
        private final List<IndexableField> nonAtomicFields;
        private IndexableField[] shapeFields = null;
        private IndexableField pointField = null;

        private FieldMaker(boolean atomic) {
            this.atomic = atomic;
            nonAtomicFields = atomic ? null : new ArrayList<>();
        }

        @Override
        public void point(Point point) {
            pointField = new LatLonPoint(BKDTree.POINT_FIELD_NAME, point.getLat(), point.getLon());
            if (!atomic) {
                nonAtomicFields.add(pointField);
            }
        }

        @Override
        public void line(Line line) {
            shapeFields = LatLonShape.createIndexableFields(BKDTree.SHAPE_FIELD_NAME, line);
            if (!atomic) {
                nonAtomicFields.addAll(Arrays.asList(shapeFields));
            }
        }

        @Override
        public void polygon(Polygon polygon) {
            shapeFields = LatLonShape.createIndexableFields(BKDTree.SHAPE_FIELD_NAME, polygon);
            if (!atomic) {
                nonAtomicFields.addAll(Arrays.asList(shapeFields));
            }
        }

    }

}
