package com.github.amirbaratpoor.lucene.bkdtree.transform;

import com.github.amirbaratpoor.lucene.bkdtree.BKDTree;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.POINT_FIELD_NAME;
import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.SHAPE_FIELD_NAME;
import static org.apache.lucene.document.ShapeField.QueryRelation.CONTAINS;

class ContainsQueryMaker extends GeometryTransformer implements QueryMaker {

    private final BooleanQuery.Builder builder = new BooleanQuery.Builder();
    private Query query;

    public ContainsQueryMaker(Geometry geometry) {
        super(geometry);
    }

    @Override
    public void onPoint(Point point) {
        Query pointQuery = LatLonPoint.newGeometryQuery(POINT_FIELD_NAME, CONTAINS, point);
        Query shapeQuery = LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, CONTAINS, point);
        BooleanQuery.Builder innerBool = new BooleanQuery.Builder();
        innerBool.add(pointQuery, BooleanClause.Occur.SHOULD);
        innerBool.add(shapeQuery, BooleanClause.Occur.SHOULD);
        this.query = innerBool.build();
        if (!atomic) {
            this.builder.add(this.query, BooleanClause.Occur.FILTER);
        }
    }

    @Override
    public void onLine(Line line) {
        this.query = LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, CONTAINS, line);
        if (!atomic) {
            this.builder.add(this.query, BooleanClause.Occur.MUST);
        }
    }

    @Override
    public void onPolygon(Polygon polygon) {
        this.query = LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, CONTAINS, polygon);
        if (!atomic) {
            this.builder.add(this.query, BooleanClause.Occur.MUST);
        }
    }

    @Override
    public Query makeQuery() {
        transform(geometry, this);
        Query result = atomic ? query : builder.build();
        return new ConstantScoreQuery(result);
    }

}
