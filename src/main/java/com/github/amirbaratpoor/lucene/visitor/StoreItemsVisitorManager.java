package com.github.amirbaratpoor.lucene.visitor;

import java.util.List;

public class StoreItemsVisitorManager<T> implements VisitorManager<T, StoreItemsVisitor<T>, List<T>> {

    private final ThresholdHolder thresholdHolder;

    public StoreItemsVisitorManager(ThresholdHolder thresholdHolder) {
        this.thresholdHolder = thresholdHolder;
    }

    @Override
    public StoreItemsVisitor<T> newVisitor() {
        return new StoreItemsVisitor<>(thresholdHolder);
    }

    @Override
    public List<T> reduce(List<StoreItemsVisitor<T>> visitors) {
        return visitors.stream()
                .map(StoreItemsVisitor::getItems)
                .flatMap(List::stream)
                .toList();
    }
}
