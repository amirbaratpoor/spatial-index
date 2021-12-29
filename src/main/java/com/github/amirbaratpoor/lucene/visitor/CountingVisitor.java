package com.github.amirbaratpoor.lucene.visitor;


import java.io.IOException;

public class CountingVisitor implements Visitor<Object> {
    private int count = 0;

    public int getCount() {
        return count;
    }

    @Override
    public void visit(int docId, Object item) {
        throw new AssertionError("Unreachable Statement");
    }

    @Override
    public VisitMode visitMode() {
        return VisitMode.COMPLETE;
    }


    @Override
    public boolean collect(int docId) throws IOException {
        ++count;
        return false;
    }
}
