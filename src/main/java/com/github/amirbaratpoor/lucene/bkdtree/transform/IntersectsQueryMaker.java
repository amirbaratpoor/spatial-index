package com.github.amirbaratpoor.lucene.bkdtree.transform;

import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

public class IntersectsQueryMaker extends GeometryTransformer implements QueryMaker{
    protected IntersectsQueryMaker(Geometry geometry) {
        super(geometry);
    }

    @Override
    void onPoint(Point point) {

    }

    @Override
    void onLine(Line line) {

    }

    @Override
    void onPolygon(Polygon polygon) {

    }

    @Override
    public Query makeQuery() {
        return null;
    }
}
