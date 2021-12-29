package com.github.amirbaratpoor.lucene;

import com.github.amirbaratpoor.io.Deserializer;
import com.github.amirbaratpoor.io.JavaDeserializer;
import com.github.amirbaratpoor.io.JavaSerializer;
import com.github.amirbaratpoor.io.Serializer;
import com.github.amirbaratpoor.lucene.collect.VisitorCollector;
import com.github.amirbaratpoor.lucene.collect.VisitorManagerCollectorManager;
import com.github.amirbaratpoor.lucene.visitor.Visitor;
import com.github.amirbaratpoor.lucene.visitor.VisitorManager;
import org.apache.lucene.codecs.lucene90.Lucene90Codec;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.IOUtils;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractSpatialIndex<T> implements SpatialIndex<T> {
    protected final IndexWriter indexWriter;
    protected final SearcherManager searcherManager;
    protected final Deserializer<? extends T> deserializer;
    protected final Serializer<? super T> serializer;
    protected final AtomicBoolean closed;

    protected AbstractSpatialIndex(Builder<T, ?> builder) throws IOException {
        IndexWriter indexWriter = null;
        SearcherManager searcherManager = null;
        SearcherFactory searcherFactory = getSearcherFactory(builder.executor);
        try {

            indexWriter = new IndexWriter(builder.directory, getConfig(builder));
            searcherManager = new SearcherManager(indexWriter, searcherFactory);
            this.deserializer = Objects.requireNonNullElseGet(builder.deserializer, JavaDeserializer::getInstance);
            this.serializer = Objects.requireNonNullElseGet(builder.serializer, JavaSerializer::getInstance);
            this.closed = new AtomicBoolean(false);
            this.indexWriter = indexWriter;
            this.searcherManager = searcherManager;

        } catch (Throwable t) {
            IOUtils.closeWhileHandlingException(indexWriter, searcherManager);
            throw t;
        }

    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndExchangeRelease(false, true)) {
            IOUtils.close(indexWriter, searcherManager);
        }
    }

    private void ensureOpen() {
        if (closed.getAcquire()) {
            throw new IllegalStateException("This Index is closed");
        }
    }


    @Override
    public <V extends Visitor<? super T>, R> R queryById(Geometry searchShape, VisitorManager<T, V, R> visitorManager) throws IOException {
        return null;
    }

    @Override
    public void queryById(String id, Visitor<? super T> visitor) throws IOException {
        ensureOpen();
        executeQuery(createQuery(id), visitor);
    }


    @Override
    public <V extends Visitor<? super T>, R> R query(Geometry searchShape, Relation relation, VisitorManager<T, V, R> visitorManager) throws IOException {
        ensureOpen();
        return executeQuery(createQuery(searchShape, relation), visitorManager);
    }

    @Override
    public <V extends Visitor<? super T>> void query(Geometry searchShape, Relation relation, V visitor) throws IOException {
        ensureOpen();
        executeQuery(createQuery(searchShape, relation), visitor);
    }

    @Override
    public void remove(Geometry shape, Relation relation) throws IOException {
        ensureOpen();
        indexWriter.deleteDocuments(createQuery(shape, relation));
    }

    @Override
    public void commit() throws IOException {
        ensureOpen();
        indexWriter.commit();
    }

    @Override
    public void rollback() throws IOException {
        if (!closed.compareAndExchangeRelease(false, true)) {
            IOUtils.close(searcherManager, indexWriter::rollback);
        }
    }

    @Override
    public void refresh() throws IOException {
        searcherManager.maybeRefreshBlocking();
    }

    @Override
    public void removeById(String id) throws IOException {
        ensureOpen();
        indexWriter.deleteDocuments(createQuery(id));
    }

    @Override
    public void removeAll() throws IOException {
        ensureOpen();
        indexWriter.deleteAll();
    }

    @Override
    public void insert(String id, Geometry shape, T source) {

    }

    private <V extends Visitor<? super T>> void executeQuery(Query query, V visitor) throws IOException {
        VisitorCollector<T, V> collector = new VisitorCollector<>(visitor, deserializer);
        final IndexSearcher ref = searcherManager.acquire();
        try {
            ref.search(query, collector);
        } finally {
            searcherManager.release(ref);
        }
    }

    private <V extends Visitor<? super T>, R> R executeQuery(Query query, VisitorManager<T, V, R> visitorManager) throws IOException {
        VisitorManagerCollectorManager<T, V, R> collectorManager = new VisitorManagerCollectorManager<>(visitorManager, deserializer);
        final IndexSearcher ref = searcherManager.acquire();
        try {
            return ref.search(query, collectorManager);
        } finally {
            searcherManager.release(ref);
        }
    }

    @Override
    public int parallelism() throws IOException {
        ensureOpen();
        final IndexSearcher ref = searcherManager.acquire();
        try {
            IndexSearcher.LeafSlice[] slices = ref.getSlices();
            return slices == null ? 0 : slices.length - 1;
        } finally {
            searcherManager.release(ref);
        }
    }

    protected abstract Query createQuery(Geometry searchShape, Relation relation);

    protected Query createQuery(String id) {
        return new TermQuery(new Term("id", id));
    }


    private SearcherFactory getSearcherFactory(Executor executor) {
        return new SearcherFactory() {
            @Override
            public IndexSearcher newSearcher(IndexReader reader, IndexReader previousReader) {
                IndexSearcher indexSearcher = new IndexSearcher(reader, executor);
                indexSearcher.setQueryCachingPolicy(null);
                return indexSearcher;
            }
        };
    }

    private IndexWriterConfig getConfig(Builder<T, ?> builder) {
        return new IndexWriterConfig()
                .setCodec(new Lucene90Codec(Lucene90Codec.Mode.BEST_SPEED))
                .setOpenMode(builder.openMode)
                .setRAMBufferSizeMB(builder.ramBufferSizeMB);
    }

    protected abstract static class Builder<T, B extends Builder<T, B>> {
        private final Directory directory;
        private IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
        private Executor executor = null;
        private double ramBufferSizeMB = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
        private Deserializer<? extends T> deserializer;
        private Serializer<? super T> serializer;


        protected Builder(Directory directory) {
            this.directory = Objects.requireNonNull(directory);
        }

        public B setExecutor(Executor executor) {
            this.executor = executor;
            return self();
        }

        public B setOpenMode(IndexWriterConfig.OpenMode openMode) {
            this.openMode = openMode;
            return self();
        }

        public B setDeserializer(Deserializer<? extends T> deserializer) {
            this.deserializer = deserializer;
            return self();
        }

        public B setSerializer(Serializer<? super T> serializer) {
            this.serializer = serializer;
            return self();
        }

        public B setRAMBufferSizeMB(double ramBufferSizeMB) {
            this.ramBufferSizeMB = ramBufferSizeMB;
            return self();
        }

        protected abstract B self();

        protected abstract AbstractSpatialIndex<T> build() throws IOException;
    }
}
