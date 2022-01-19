package com.github.amirbaratpoor.lucene;

import com.github.amirbaratpoor.lucene.visitor.Visitor;
import com.github.amirbaratpoor.lucene.visitor.VisitorManager;
import org.locationtech.jts.geom.Geometry;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public interface SpatialIndex<T> extends Closeable {

    Collection<T> query(Geometry searchShape, Relation relation, int size) throws IOException;

    <V extends Visitor<? super T>> void query(Geometry searchShape, Relation relation, V visitor) throws IOException;

    <V extends Visitor<? super T>, R> R query(Geometry searchShape, Relation relation, VisitorManager<T, V, R> visitorManager) throws IOException;

    Collection<T> queryById(String id, int size) throws IOException;

    void queryById(String id, Visitor<? super T> visitor) throws IOException;

    <V extends Visitor<? super T>, R> R queryById(String id, VisitorManager<T, V, R> visitorManager) throws IOException;

    void remove(String id) throws IOException;

    void remove(Geometry shape, Relation relation) throws IOException;

    void removeAll() throws IOException;

    void insert(String id, Geometry shape, T source) throws IOException;

    void insert(Geometry shape, T source) throws IOException;

    int parallelism() throws IOException;

    void commit() throws IOException;

    void rollback() throws IOException;

    void refresh() throws IOException;

}
