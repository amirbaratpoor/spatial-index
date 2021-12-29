package com.github.amirbaratpoor.lucene.collect;

import com.github.amirbaratpoor.io.Deserializer;
import com.github.amirbaratpoor.lucene.visitor.Visitor;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class VisitorCollector<T, V extends Visitor<? super T>> extends SimpleCollector {

    public static final String SOURCE_FIELD = "source";
    private final V visitor;
    private final Deserializer<? extends T> deserializer;
    private BinaryDocValues docValues;

    public VisitorCollector(V visitor, Deserializer<? extends T> deserializer) {
        this.visitor = visitor;
        this.deserializer = deserializer;
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) throws IOException {
        this.docValues = DocValues.getBinary(context.reader(), SOURCE_FIELD);
        visitor.doSetNextReader(context);
    }

    @Override
    public void collect(int doc) throws IOException {
        if (!visitor.collect(doc)) {
            return;
        }
        docValues.advanceExact(doc);
        BytesRef bytesRef = docValues.binaryValue();
        if (bytesRef != null && bytesRef.isValid()) {
            T source = deserializer.deserialize(bytesRef.bytes);
            visitor.visit(doc, source);
        }
    }

    public V getVisitor() {
        return visitor;
    }

    @Override
    public ScoreMode scoreMode() {
        return visitor.visitMode().getScoreMode();
    }
}
