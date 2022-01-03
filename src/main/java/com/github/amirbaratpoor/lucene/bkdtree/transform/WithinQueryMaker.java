package com.github.amirbaratpoor.lucene.bkdtree.transform;

import com.github.amirbaratpoor.lucene.bkdtree.BKDTree;
import com.github.amirbaratpoor.lucene.bkdtree.transform.GeometryTransformer;
import com.github.amirbaratpoor.lucene.bkdtree.transform.QueryMaker;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

import static org.apache.lucene.document.ShapeField.QueryRelation.WITHIN;

class WithinQueryMaker extends GeometryTransformer implements QueryMaker {

    private final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    private Query query;

    public WithinQueryMaker(Geometry geometry) {
        super(geometry);

    }

    @Override
    public void onPoint(Point point) {
        this.query = LatLonPoint.newGeometryQuery(BKDTree.POINT_FIELD_NAME, WITHIN, point);
        if (!atomic) {
            builder.add(this.query, Occur.FILTER);
        }
    }

    @Override
    public void onLine(Line line) {
        throw new IllegalArgumentException("Relation.WITHIN does not support Lines");
    }

    @Override
    public void onPolygon(Polygon polygon) {
        BooleanQuery.Builder innerBuilder = new BooleanQuery.Builder();
        innerBuilder.add(LatLonPoint.newGeometryQuery(BKDTree.POINT_FIELD_NAME, WITHIN, polygon), Occur.FILTER);
        innerBuilder.add(LatLonShape.newGeometryQuery(BKDTree.SHAPE_FIELD_NAME, WITHIN, polygon), Occur.FILTER);
        this.query = innerBuilder.build();
        if (!atomic) {
            builder.add(this.query, Occur.FILTER);
        }
    }

    @Override
    public Query makeQuery() {
        GeometryTransformer.transform(geometry, this);
        if (atomic) {
            return query;
        }
        return builder.build();
    }
}
