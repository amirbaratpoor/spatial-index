package com.github.amirbaratpoor.lucene.collect;

import com.github.amirbaratpoor.io.Deserializer;
import com.github.amirbaratpoor.lucene.visitor.Visitor;
import com.github.amirbaratpoor.lucene.visitor.VisitorManager;
import org.apache.lucene.search.CollectorManager;

import java.io.IOException;
import java.util.Collection;

public class VisitorManagerCollectorManager<T, V extends Visitor<? super T>, R> implements CollectorManager<VisitorCollector<T, V>, R> {

    private final VisitorManager<T, V, R> visitorManager;
    private final Deserializer<? extends T> deserializer;

    public VisitorManagerCollectorManager(VisitorManager<T, V, R> visitorManager, Deserializer<? extends T> deserializer) {
        this.visitorManager = visitorManager;
        this.deserializer = deserializer;
    }


    @Override
    public VisitorCollector<T, V> newCollector() throws IOException {
        return new VisitorCollector<>(visitorManager.newVisitor(), deserializer);
    }

    @Override
    public R reduce(Collection<VisitorCollector<T, V>> collectors) throws IOException {
        return visitorManager.reduce(
                collectors.stream().map(VisitorCollector::getVisitor).toList()
        );
    }
}
