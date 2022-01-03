package com.github.amirbaratpoor.lucene.bkdtree;

import com.github.amirbaratpoor.lucene.AbstractSpatialIndex;
import com.github.amirbaratpoor.lucene.Relation;
import com.github.amirbaratpoor.lucene.bkdtree.transform.FieldMaker;
import com.github.amirbaratpoor.lucene.bkdtree.transform.QueryMaker;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;

public class BKDTree<T> extends AbstractSpatialIndex<T> {

    public static final String POINT_FIELD_NAME = "geo_point_bkd";
    public static final String SHAPE_FIELD_NAME = "geo_shape_bkd";

    private BKDTree(Builder<T> builder) throws IOException {
        super(builder);
    }

    @Override
    protected Query shapeQuery(Geometry searchShape, Relation relation) {
        return QueryMaker.create(relation, searchShape).makeQuery();
    }

    @Override
    protected IndexableField[] shapeFields(Geometry shape) {
        return FieldMaker.create(shape).makeFields();
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
