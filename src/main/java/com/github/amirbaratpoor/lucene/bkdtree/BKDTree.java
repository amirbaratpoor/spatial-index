package com.github.amirbaratpoor.lucene.bkdtree;

import com.github.amirbaratpoor.lucene.AbstractSpatialIndex;
import com.github.amirbaratpoor.lucene.Relation;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.Map;

public class BKDTree<T> extends AbstractSpatialIndex<T> {

    public static final String POINT_FIELD_NAME = "geo_point_bkd";
    public static final String SHAPE_FIELD_NAME = "geo_shape_bkd";
    private static final BKDFieldMaker fieldMaker = new BKDFieldMaker();
    private static final Map<Relation, BKDQueryMaker> queryMakers = Map.of(
            Relation.CONTAINS, new ContainsQueryMaker(),
            Relation.WITHIN, new WithinQueryMaker(),
            Relation.DISJOINT, RelationQueryMaker.newDisjointQueryMaker(),
            Relation.INTERSECTS, RelationQueryMaker.newIntersectsQueryMaker()
    );

    private BKDTree(Builder<T> builder) throws IOException {
        super(builder);
    }

    private static Query envelopeQuery(Relation relation, Envelope envelope) {
        Query shapeQuery = LatLonShape.newBoxQuery(BKDTree.SHAPE_FIELD_NAME, relation.getQueryRelation(),
                envelope.getMinY(), envelope.getMaxY(),
                envelope.getMinX(), envelope.getMaxX());
        if (relation == Relation.CONTAINS) {
            return new ConstantScoreQuery(shapeQuery);
        }
        Query pointQuery = LatLonPoint.newBoxQuery(BKDTree.POINT_FIELD_NAME,
                envelope.getMinY(), envelope.getMaxY(),
                envelope.getMinX(), envelope.getMaxX());
        BooleanClause.Occur occur = relation == Relation.INTERSECTS ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.FILTER;
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(shapeQuery, occur);
        builder.add(pointQuery, occur);
        return new ConstantScoreQuery(builder.build());

    }

    @Override
    protected Query shapeQuery(Geometry searchShape, Relation relation) {
        return queryMakers.get(relation).makeQuery(searchShape);
    }

    @Override
    protected IndexableField[] shapeFields(Geometry shape) {
        return fieldMaker.makeFields(shape);
    }

    public static class Builder<T> extends AbstractSpatialIndex.Builder<T, Builder<T>> {

        public Builder(Directory directory) {
            super(directory);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        public BKDTree<T> build() throws IOException {
            return new BKDTree<>(this);
        }
    }
}
