package com.github.amirbaratpoor.lucene.bkdtree;

import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

@FunctionalInterface
interface BKDQueryMaker {

    Query makeQuery(Geometry geometry);

}
