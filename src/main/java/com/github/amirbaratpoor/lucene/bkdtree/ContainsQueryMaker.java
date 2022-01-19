package com.github.amirbaratpoor.lucene.bkdtree;

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

final class ContainsQueryMaker implements BKDQueryMaker {

    @Override
    public Query makeQuery(Geometry geometry) {
        boolean atomic = GeometryTransformer.isAtomic(geometry);
        QueryMaker queryMaker = new QueryMaker(atomic);
        GeometryTransformer.consume(geometry, queryMaker);
        Query result = atomic ? queryMaker.query : queryMaker.builder.build();
        return new ConstantScoreQuery(result);
    }

    private static class QueryMaker implements LatLonConsumer {
        private final BooleanQuery.Builder builder;
        private final boolean atomic;
        private Query query;

        private QueryMaker(boolean atomic) {
            this.atomic = atomic;
            this.builder = atomic ? null : new BooleanQuery.Builder();
        }

        @Override
        public void point(Point point) {
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
        public void line(Line line) {
            this.query = LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, CONTAINS, line);
            if (!atomic) {
                this.builder.add(this.query, BooleanClause.Occur.FILTER);
            }
        }

        @Override
        public void polygon(Polygon polygon) {
            this.query = LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, CONTAINS, polygon);
            if (!atomic) {
                this.builder.add(this.query, BooleanClause.Occur.FILTER);
            }
        }
    }
}
