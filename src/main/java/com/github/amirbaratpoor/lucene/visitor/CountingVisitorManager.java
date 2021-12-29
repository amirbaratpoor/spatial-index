package com.github.amirbaratpoor.lucene.visitor;

import java.util.List;

public class CountingVisitorManager implements VisitorManager<Object, CountingVisitor, Integer> {
    @Override
    public CountingVisitor newVisitor() {
        return new CountingVisitor();
    }

    @Override
    public Integer reduce(List<CountingVisitor> visitors) {
        return visitors.stream()
                .mapToInt(CountingVisitor::getCount)
                .sum();
    }
}
