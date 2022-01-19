package com.github.amirbaratpoor.lucene;

import com.github.amirbaratpoor.io.Deserializer;
import com.github.amirbaratpoor.io.JavaDeserializer;
import com.github.amirbaratpoor.io.JavaSerializer;
import com.github.amirbaratpoor.io.Serializer;
import com.github.amirbaratpoor.lucene.collect.VisitorCollector;
import com.github.amirbaratpoor.lucene.collect.VisitorManagerCollectorManager;
import com.github.amirbaratpoor.lucene.visitor.ListVisitor;
import com.github.amirbaratpoor.lucene.visitor.ThresholdHolder;
import com.github.amirbaratpoor.lucene.visitor.Visitor;
import com.github.amirbaratpoor.lucene.visitor.VisitorManager;
import org.apache.lucene.codecs.lucene90.Lucene90Codec;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public abstract class AbstractSpatialIndex<T> implements SpatialIndex<T> {

    public static final String ID_FIELD_NAME = "id";
    public static final String SOURCE_FIELD_NAME = "source";

    protected final IndexWriter indexWriter;
    protected final SearcherManager searcherManager;
    protected final Deserializer<? extends T> deserializer;
    protected final Serializer<? super T> serializer;

    protected AbstractSpatialIndex(Builder<T, ?> builder) throws IOException {
        IndexWriter indexWriter = null;
        SearcherManager searcherManager = null;
        SearcherFactory searcherFactory = getSearcherFactory(builder.executor);
        try {

            indexWriter = new IndexWriter(builder.directory, getConfig(builder));
            searcherManager = new SearcherManager(indexWriter, searcherFactory);
            this.deserializer = Objects.requireNonNullElseGet(builder.deserializer, JavaDeserializer::getInstance);
            this.serializer = Objects.requireNonNullElseGet(builder.serializer, JavaSerializer::getInstance);
            this.indexWriter = indexWriter;
            this.searcherManager = searcherManager;

        } catch (Throwable t) {
            IOUtils.closeWhileHandlingException(indexWriter, searcherManager);
            throw t;
        }

    }

    @Override
    public synchronized void close() throws IOException {
        if (indexWriter.isOpen()) {
            IOUtils.close(indexWriter, searcherManager);
        }
    }

    @Override
    public List<T> query(Geometry searchShape, Relation relation, int size) throws IOException {
        ListVisitor<T> visitor = new ListVisitor<>(ThresholdHolder.createMutable(size));
        query(searchShape, relation, visitor);
        return visitor.getItems();
    }

    @Override
    public <V extends Visitor<? super T>> void query(Geometry searchShape, Relation relation, V visitor) throws IOException {
        executeQuery(shapeQuery(searchShape, relation), visitor);
    }

    @Override
    public <V extends Visitor<? super T>, R> R query(Geometry searchShape, Relation relation, VisitorManager<T, V, R> visitorManager) throws IOException {
        return executeQuery(shapeQuery(searchShape, relation), visitorManager);
    }

    @Override
    public List<T> queryById(String id, int size) throws IOException {
        ListVisitor<T> visitor = new ListVisitor<>(ThresholdHolder.createMutable(size));
        queryById(id, visitor);
        return visitor.getItems();
    }

    @Override
    public void queryById(String id, Visitor<? super T> visitor) throws IOException {
        executeQuery(idQuery(id), visitor);
    }

    @Override
    public <V extends Visitor<? super T>, R> R queryById(String id, VisitorManager<T, V, R> visitorManager) throws IOException {
        return executeQuery(idQuery(id), visitorManager);
    }

    @Override
    public void remove(String id) throws IOException {
        indexWriter.deleteDocuments(idQuery(id));
    }

    @Override
    public void remove(Geometry shape, Relation relation) throws IOException {
        indexWriter.deleteDocuments(shapeQuery(shape, relation));
    }

    @Override
    public void removeAll() throws IOException {
        indexWriter.deleteAll();
    }

    @Override
    public void insert(String id, Geometry shape, T source) throws IOException {
        IndexableField[] shapeFields = shapeFields(shape);
        Document document = new Document();
        for (IndexableField shapeField : shapeFields) {
            document.add(shapeField);
        }
        document.add(sourceField(source));
        document.add(idField(id));
        indexWriter.addDocument(document);
    }

    @Override
    public void insert(Geometry shape, T source) throws IOException {
        insert(null, shape, source);
    }

    @Override
    public int parallelism() throws IOException {
        final IndexSearcher ref = searcherManager.acquire();
        try {
            IndexSearcher.LeafSlice[] slices = ref.getSlices();
            return slices == null ? 0 : slices.length - 1;
        } finally {
            searcherManager.release(ref);
        }
    }

    @Override
    public void commit() throws IOException {
        indexWriter.commit();
    }

    @Override
    public synchronized void rollback() throws IOException {
        if (indexWriter.isOpen()) {
            IOUtils.close(searcherManager, indexWriter::rollback);
        }

    }

    @Override
    public void refresh() throws IOException {
        searcherManager.maybeRefreshBlocking();
    }

    private <V extends Visitor<? super T>> void executeQuery(Query query, V visitor) throws IOException {
        VisitorCollector<T, V> collector = new VisitorCollector<>(visitor, SOURCE_FIELD_NAME, deserializer);
        final IndexSearcher ref = searcherManager.acquire();
        try {
            ref.search(query, collector);
        } finally {
            searcherManager.release(ref);
        }
    }

    private <V extends Visitor<? super T>, R> R executeQuery(Query query, VisitorManager<T, V, R> visitorManager) throws IOException {
        VisitorManagerCollectorManager<T, V, R> collectorManager = new VisitorManagerCollectorManager<>(visitorManager, SOURCE_FIELD_NAME, deserializer);
        final IndexSearcher ref = searcherManager.acquire();
        try {
            return ref.search(query, collectorManager);
        } finally {
            searcherManager.release(ref);
        }
    }

    protected abstract Query shapeQuery(Geometry searchShape, Relation relation);

    protected abstract IndexableField[] shapeFields(Geometry shape);

    private Query idQuery(String id) {
        return new TermQuery(new Term(ID_FIELD_NAME, id));
    }

    private IndexableField idField(String id) {
        return new StringField(ID_FIELD_NAME, id, Field.Store.NO);
    }

    private IndexableField sourceField(T source) throws IOException {
        byte[] bytes = serializer.serialize(source);
        return new BinaryDocValuesField(SOURCE_FIELD_NAME, new BytesRef(bytes));
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
