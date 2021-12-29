package com.github.amirbaratpoor.lucene.visitor;

import java.io.IOException;
import java.util.List;

public interface VisitorManager<T, V extends Visitor<? super T>, R> {

    V newVisitor() throws IOException;

    R reduce(List<V> visitors) throws IOException;

}
