package com.github.amirbaratpoor.lucene.visitor;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;

public interface Visitor<T> {

    void visit(int docId, T item) throws IOException;

    VisitMode visitMode();

    boolean collect(int docId) throws IOException;

    default void doSetNextReader(LeafReaderContext context) throws IOException {

    }

    enum VisitMode {
        COMPLETE(ScoreMode.COMPLETE_NO_SCORES), TOP_DOCS(ScoreMode.TOP_DOCS);

        final ScoreMode scoreMode;

        VisitMode(ScoreMode scoreMode) {
            this.scoreMode = scoreMode;
        }

        public ScoreMode getScoreMode() {
            return scoreMode;
        }
    }

}
