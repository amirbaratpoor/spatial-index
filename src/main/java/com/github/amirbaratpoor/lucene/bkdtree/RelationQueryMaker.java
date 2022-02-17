package com.github.amirbaratpoor.lucene.bkdtree;

import com.github.amirbaratpoor.lucene.Relation;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.geo.LatLonGeometry;
import org.apache.lucene.geo.Line;
import org.apache.lucene.geo.Point;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.POINT_FIELD_NAME;
import static com.github.amirbaratpoor.lucene.bkdtree.BKDTree.SHAPE_FIELD_NAME;
import static org.apache.lucene.search.BooleanClause.Occur.MUST_NOT;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

class RelationQueryMaker implements BKDQueryMaker {
    private final Relation relation;

    private RelationQueryMaker(Relation relation) {
        this.relation = relation;
    }

    public static BKDQueryMaker newIntersectsQueryMaker() {
        return new RelationQueryMaker(Relation.INTERSECTS);
    }

    public static BKDQueryMaker newDisjointQueryMaker() {
        return new RelationQueryMaker(Relation.DISJOINT);
    }

    @Override
    public Query makeQuery(Geometry geometry) {
        LatLonCollector extractor = new LatLonCollector(geometry);
        GeometryTransformer.process(geometry, extractor);
        LatLonGeometry[] geometries = extractor.latLonGeometries.toArray(new LatLonGeometry[0]);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        BooleanClause.Occur occur = relation == Relation.INTERSECTS ? SHOULD : MUST_NOT;
        builder.add(LatLonPoint.newGeometryQuery(POINT_FIELD_NAME, relation.getQueryRelation(), geometries), occur);
        builder.add(LatLonShape.newGeometryQuery(SHAPE_FIELD_NAME, relation.getQueryRelation(), geometries), occur);
        return new ConstantScoreQuery(builder.build());
    }

    private static class LatLonCollector implements LatLonConsumer {
        private final boolean atomic;
        private final Geometry geometry;
        private List<LatLonGeometry> latLonGeometries;

        public LatLonCollector(Geometry geometry) {
            this.atomic = GeometryTransformer.isAtomic(geometry);
            this.geometry = geometry;
        }

        @Override
        public void point(Point point) {
            if (atomic) {
                latLonGeometries = Collections.singletonList(point);
            }
            initialize();
            latLonGeometries.add(point);
        }

        @Override
        public void line(Line line) {
            if (atomic) {
                latLonGeometries = Collections.singletonList(line);
            }
            initialize();
            latLonGeometries.add(line);
        }

        @Override
        public void polygon(Polygon polygon) {
            if (atomic) {
                latLonGeometries = Collections.singletonList(polygon);
            }
            initialize();
            latLonGeometries.add(polygon);
        }

        private void initialize() {
            if (latLonGeometries == null) {
                if (GeometryTransformer.isSimpleCollection(geometry)) {
                    latLonGeometries = new ArrayList<>(geometry.getNumGeometries());
                } else {
                    latLonGeometries = new ArrayList<>();
                }
            }
        }

    }
}
