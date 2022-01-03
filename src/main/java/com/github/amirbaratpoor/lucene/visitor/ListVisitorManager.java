package com.github.amirbaratpoor.lucene.visitor;

import java.util.List;

public class ListVisitorManager<T> implements VisitorManager<T, ListVisitor<T>, List<T>> {

    private final ThresholdHolder thresholdHolder;

    public ListVisitorManager(ThresholdHolder thresholdHolder) {
        this.thresholdHolder = thresholdHolder;
    }

    @Override
    public ListVisitor<T> newVisitor() {
        return new ListVisitor<>(thresholdHolder);
    }

    @Override
    public List<T> reduce(List<ListVisitor<T>> visitors) {
        return visitors.stream()
                .map(ListVisitor::getItems)
                .flatMap(List::stream)
                .toList();
    }
}
