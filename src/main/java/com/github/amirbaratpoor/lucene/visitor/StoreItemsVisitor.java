package com.github.amirbaratpoor.lucene.visitor;

import org.apache.lucene.search.CollectionTerminatedException;

import java.util.ArrayList;
import java.util.List;

public class StoreItemsVisitor<T> implements Visitor<T> {

    private final List<T> items = new ArrayList<>();
    private final ThresholdHolder thresholdHolder;

    public StoreItemsVisitor(ThresholdHolder thresholdHolder) {
        this.thresholdHolder = thresholdHolder;
    }

    @Override
    public void visit(int docId, T item) {
        items.add(item);
    }

    @Override
    public VisitMode visitMode() {
        return VisitMode.COMPLETE;
    }

    @Override
    public boolean collect(int docId) {
        int hitCount = thresholdHolder.incrementAndGet();
        if (hitCount > thresholdHolder.getThreshold()) {
            throw new CollectionTerminatedException();
        }
        return true;
    }

    public List<T> getItems() {
        return items;
    }
}
