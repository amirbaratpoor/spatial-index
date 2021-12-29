package com.github.amirbaratpoor.lucene;

import com.github.amirbaratpoor.lucene.visitor.StoreItemsVisitor;
import com.github.amirbaratpoor.lucene.visitor.ThresholdHolder;
import com.github.amirbaratpoor.lucene.visitor.Visitor;
import com.github.amirbaratpoor.lucene.visitor.VisitorManager;
import org.apache.lucene.util.IOUtils.IOFunction;
import org.locationtech.jts.geom.Geometry;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public interface SpatialIndex<T> extends Closeable {

    default Collection<T> queryById(String id, int size) throws IOException {
        ThresholdHolder thresholdHolder = ThresholdHolder.createMutable(size);
        StoreItemsVisitor<T> visitor = new StoreItemsVisitor<>(thresholdHolder);
        queryById(id, visitor);
        return visitor.getItems();
    }

    <V extends Visitor<? super T>, R> R queryById(Geometry searchShape, VisitorManager<T, V, R> visitorManager) throws IOException;

    void queryById(String id, Visitor<? super T> visitor) throws IOException;

    default <V extends Visitor<? super T>, R> R queryById(String id, V visitor, IOFunction<V, R> extractor) throws IOException {
        queryById(id, visitor);
        return extractor.apply(visitor);
    }

    void removeById(String id) throws IOException;

    void insert(String id, Geometry shape, T source);

    int parallelism() throws IOException;

    default Collection<T> query(Geometry searchShape, Relation relation, int size) throws IOException {
        ThresholdHolder thresholdHolder = ThresholdHolder.createMutable(size);
        StoreItemsVisitor<T> visitor = new StoreItemsVisitor<>(thresholdHolder);
        query(searchShape, relation, visitor);
        return visitor.getItems();
    }

    <V extends Visitor<? super T>, R> R query(Geometry searchShape, Relation relation, VisitorManager<T, V, R> visitorManager) throws IOException;

    <V extends Visitor<? super T>> void query(Geometry searchShape, Relation relation, V visitor) throws IOException;

    default <V extends Visitor<? super T>, R> R query(Geometry searchShape, Relation relation, V visitor, IOFunction<V, R> extractor) throws IOException {
        query(searchShape, relation, visitor);
        return extractor.apply(visitor);
    }

    default void insert(Geometry shape, T source){
        insert(null, shape, source);
    }

    void remove(Geometry shape, Relation relation) throws IOException;

    void removeAll() throws IOException;

    void commit() throws IOException;

    void rollback() throws IOException;

    void refresh() throws IOException;

}
