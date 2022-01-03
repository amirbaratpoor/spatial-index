package com.github.amirbaratpoor.lucene.bkdtree.transform;

import com.github.amirbaratpoor.lucene.Relation;
import com.github.amirbaratpoor.lucene.bkdtree.BKDTree;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.EnumMap;
import java.util.Map;

public interface QueryMaker {
    Map<Relation, Factory> GEOMETRY_QUERY_MAKER_FACTORIES = new EnumMap<>(Map.of(
            Relation.CONTAINS, ContainsQueryMaker::new,
            Relation.WITHIN, WithinQueryMaker::new,
            Relation.DISJOINT, DisjointQueryMaker::new,
            Relation.INTERSECTS, IntersectsQueryMaker::new
    ));

    static QueryMaker create(Relation relation, Geometry geometry) {
        return GEOMETRY_QUERY_MAKER_FACTORIES.get(relation).newQueryMaker(geometry);
    }

    static QueryMaker create(Relation relation, Envelope envelope) {
        Query shapeQuery = LatLonShape.newBoxQuery(BKDTree.SHAPE_FIELD_NAME, relation.getQueryRelation(),
                envelope.getMinY(), envelope.getMaxY(),
                envelope.getMinX(), envelope.getMaxX());
        if (relation == Relation.CONTAINS) {
            return () -> shapeQuery;
        }
        Query pointQuery = LatLonPoint.newBoxQuery(BKDTree.POINT_FIELD_NAME,
                envelope.getMinY(), envelope.getMaxY(),
                envelope.getMinX(), envelope.getMaxX());
        Occur occur = relation == Relation.INTERSECTS ? Occur.SHOULD : Occur.FILTER;
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(shapeQuery, occur);
        builder.add(pointQuery, occur);
        return builder::build;

    }

    Query makeQuery();

    interface Factory {
        QueryMaker newQueryMaker(Geometry geometry);
    }

}
