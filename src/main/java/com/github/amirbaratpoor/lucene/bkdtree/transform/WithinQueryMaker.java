package com.github.amirbaratpoor.lucene.bkdtree.transform;

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

class WithinQueryMaker extends GeometryTransformer implements QueryMaker {

    private List<Point> points;
    private List<Polygon> polygons;

    public WithinQueryMaker(Geometry geometry) {
        super(geometry);
    }

    @Override
    public void onPoint(Point point) {
        if (atomic) {
            this.points = Collections.singletonList(point);
            return;
        }
        initializePoints();
        points.add(point);
    }

    @Override
    public void onLine(Line line) {
        throw new IllegalArgumentException("Relation.WITHIN does not support Lines");
    }

    @Override
    public void onPolygon(Polygon polygon) {
        if (atomic) {
            this.polygons = Collections.singletonList(polygon);
            return;
        }
        initializePolygons();
        polygons.add(polygon);
    }

    private void initializePoints() {
        if (points == null) {
            if (simpleCollection) {
                points = new ArrayList<>(geometry.getNumGeometries());
            } else {
                points = new ArrayList<>();
            }
        }
    }

    private void initializePolygons() {
        if (polygons == null) {
            if (simpleCollection) {
                polygons = new ArrayList<>(geometry.getNumGeometries());
            } else {
                polygons = new ArrayList<>();
            }
        }
    }

    @Override
    public Query makeQuery() {
        Query pointsQuery = null;
        if (points != null) {
            pointsQuery = LatLonPoint.newGeometryQuery(POINT_FIELD_NAME, WITHIN, points.toArray(new Point[0]));
        }
        if (polygons != null) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Polygon[] polygons = this.polygons.toArray(new Polygon[0]);
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
}
