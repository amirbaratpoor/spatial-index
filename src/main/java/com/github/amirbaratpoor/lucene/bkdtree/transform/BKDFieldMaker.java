package com.github.amirbaratpoor.lucene.bkdtree.transform;

import com.github.amirbaratpoor.lucene.bkdtree.BKDTree;
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

class BKDFieldMaker extends GeometryTransformer implements FieldMaker {

    private final List<IndexableField> nonAtomicFields;
    private IndexableField[] shapeFields = null;
    private IndexableField pointField = null;

    protected BKDFieldMaker(Geometry geometry) {
        super(geometry);
        nonAtomicFields = atomic ? null : new ArrayList<>();
    }

    @Override
    void onPoint(Point point) {
        pointField = new LatLonPoint(BKDTree.POINT_FIELD_NAME, point.getLat(), point.getLon());
        if (!atomic) {
            nonAtomicFields.add(pointField);
        }
    }

    @Override
    void onLine(Line line) {
        shapeFields = LatLonShape.createIndexableFields(BKDTree.SHAPE_FIELD_NAME, line);
        if (!atomic) {
            nonAtomicFields.addAll(Arrays.asList(shapeFields));
        }
    }

    @Override
    void onPolygon(Polygon polygon) {
        shapeFields = LatLonShape.createIndexableFields(BKDTree.SHAPE_FIELD_NAME, polygon);
        if (!atomic) {
            nonAtomicFields.addAll(Arrays.asList(shapeFields));
        }
    }

    @Override
    public IndexableField[] makeFields() {
        if (atomic) {
            return pointField != null ? new IndexableField[]{pointField} : shapeFields;
        }
        return nonAtomicFields.toArray(new IndexableField[0]);
    }

}
