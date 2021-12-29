package com.github.amirbaratpoor.lucene.bkdtree;

import com.github.amirbaratpoor.lucene.AbstractSpatialIndex;
import com.github.amirbaratpoor.lucene.Relation;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.util.GeometryTransformer;


import java.io.IOException;

public class BKDTree<T> extends AbstractSpatialIndex<T> {

    public static final String POINT_FIELD = "geo_point_bkd";
    public static final String SHAPE_FIELD = "geo_point_shape";

    private BKDTree(Builder<T> builder) throws IOException {
        super(builder);
    }

    @Override
    protected Query createQuery(Geometry searchShape, Relation relation) {
        GeometryCollectionIterator
        return null;
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
