package com.github.amirbaratpoor.lucene.bkdtree.transform;

import com.github.amirbaratpoor.lucene.Relation;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.geo.LatLonGeometry;
import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.POINT_FIELD_NAME;
import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.SHAPE_FIELD_NAME;
import static org.apache.lucene.document.ShapeField.QueryRelation.CONTAINS;
import static org.apache.lucene.search.BooleanClause.Occur.FILTER;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

class RelationQueryMaker extends GeometryTransformer implements QueryMaker {
    private List<LatLonGeometry> latLonGeometries;
    private final Relation relation;

    private RelationQueryMaker(Geometry geometry, Relation relation) {
        super(geometry);
        this.relation = relation;
    }

    @Override
    void onPoint(Point point) {
        if (atomic) {
            latLonGeometries = Collections.singletonList(point);
        }
        initialize();
        latLonGeometries.add(point);
    }

    @Override
    void onLine(Line line) {
        if (atomic) {
            latLonGeometries = Collections.singletonList(line);
        }
        initialize();
        latLonGeometries.add(line);
    }

    @Override
    void onPolygon(Polygon polygon) {
        if (atomic) {
            latLonGeometries = Collections.singletonList(polygon);
        }
        initialize();
        latLonGeometries.add(polygon);
    }

    private void initialize() {
        if (latLonGeometries == null) {
            if (simpleCollection) {
                latLonGeometries = new ArrayList<>(geometry.getNumGeometries());
            } else {
                latLonGeometries = new ArrayList<>();
            }
        }
    }

    @Override
    public Query makeQuery() {
        LatLonGeometry[] geometries = latLonGeometries.toArray(new LatLonGeometry[0]);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        BooleanClause.Occur occur = relation == Relation.INTERSECTS ? SHOULD : FILTER;
        builder.add(LatLonPoint.newGeometryQuery(POINT_FIELD_NAME, relation.getQueryRelation(), geometries), occur);
        builder.add(LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, relation.getQueryRelation(), geometries), occur);
        return new ConstantScoreQuery(builder.build());
    }

    public static QueryMaker intersectsQueryMaker(Geometry geometry){
        return new RelationQueryMaker(geometry, Relation.CONTAINS);
    }

    public static QueryMaker disjointQueryMaker(Geometry geometry){
        return new RelationQueryMaker(geometry, Relation.DISJOINT);
    }
}
