package com.github.amirbaratpoor.lucene;

import org.apache.lucene.document.ShapeField;

public enum Relation {
    CONTAINS(ShapeField.QueryRelation.CONTAINS),
    INTERSECTS(ShapeField.QueryRelation.INTERSECTS),
    DISJOINT(ShapeField.QueryRelation.DISJOINT),
    WITHIN(ShapeField.QueryRelation.WITHIN);

    final ShapeField.QueryRelation queryRelation;

    Relation(ShapeField.QueryRelation queryRelation) {
        this.queryRelation = queryRelation;
    }

    public ShapeField.QueryRelation getQueryRelation() {
        return queryRelation;
    }
}
