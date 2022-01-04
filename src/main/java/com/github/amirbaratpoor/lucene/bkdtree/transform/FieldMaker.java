package com.github.amirbaratpoor.lucene.bkdtree.transform;

import org.apache.lucene.index.IndexableField;
import org.locationtech.jts.geom.Geometry;

public interface FieldMaker {

    IndexableField[] makeFields();

    static FieldMaker create(Geometry geometry) {
        return new BKDTreeFieldMaker(geometry);
    }
}
