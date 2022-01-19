package com.github.amirbaratpoor.lucene.bkdtree;

import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.POINT_FIELD_NAME;
import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.SHAPE_FIELD_NAME;
import static org.apache.lucene.document.ShapeField.QueryRelation.WITHIN;
import static org.apache.lucene.search.BooleanClause.Occur.FILTER;

class WithinQueryMaker implements BKDQueryMaker {

    @Override
    public Query makeQuery(Geometry geometry) {
        PointPolygonExtractor extractor = new PointPolygonExtractor(geometry);
        GeometryTransformer.consume(geometry, extractor);
        Query pointsQuery = null;
        if (extractor.points != null) {
            pointsQuery = LatLonPoint.newGeometryQuery(POINT_FIELD_NAME, WITHIN, extractor.points.toArray(new Point[0]));
        }
        if (extractor.polygons != null) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Polygon[] polygons = extractor.polygons.toArray(new Polygon[0]);
            builder.add(LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, WITHIN, polygons), FILTER);
            builder.add(LatLonPoint.newGeometryQuery(SHAPE_FIELD_NAME, WITHIN, polygons), FILTER);
            if (pointsQuery != null) {
                builder.add(pointsQuery, FILTER);
            }
            return new ConstantScoreQuery(builder.build());
        }
        assert pointsQuery != null;
        return new ConstantScoreQuery(pointsQuery);

    }

    private static class PointPolygonExtractor implements LatLonConsumer {
        private final Geometry geometry;
        private final boolean atomic;
        private List<Point> points;
        private List<Polygon> polygons;

        private PointPolygonExtractor(Geometry geometry) {
            this.geometry = geometry;
            this.atomic = GeometryTransformer.isAtomic(geometry);
        }

        @Override
        public void point(Point point) {
            if (atomic) {
                this.points = Collections.singletonList(point);
                return;
            }
            initializePoints();
            points.add(point);
        }

        @Override
        public void line(Line line) {
            throw new IllegalArgumentException("Relation.WITHIN does not support Lines");
        }

        @Override
        public void polygon(Polygon polygon) {
            if (atomic) {
                this.polygons = Collections.singletonList(polygon);
                return;
            }
            initializePolygons();
            polygons.add(polygon);
        }

        private void initializePoints() {
            if (points == null) {
                if (GeometryTransformer.isSimpleCollection(geometry)) {
                    points = new ArrayList<>(geometry.getNumGeometries());
                } else {
                    points = new ArrayList<>();
                }
            }
        }

        private void initializePolygons() {
            if (polygons == null) {
                if (GeometryTransformer.isSimpleCollection(geometry)) {
                    polygons = new ArrayList<>(geometry.getNumGeometries());
                } else {
                    polygons = new ArrayList<>();
                }
            }
        }
    }
}
