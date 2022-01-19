package com.github.amirbaratpoor.lucene.bkdtree;

import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;

public interface LatLonConsumer {

    void point(Point point);

    void line(Line line);

    void polygon(Polygon polygon);
}
