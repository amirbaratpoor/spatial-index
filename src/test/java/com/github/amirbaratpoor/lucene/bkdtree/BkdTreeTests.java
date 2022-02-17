package com.github.amirbaratpoor.lucene.bkdtree;

import com.github.amirbaratpoor.lucene.TestModel;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.IOUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BkdTreeTests {

    private Directory directory;

    @BeforeEach
    void openDirectory() {
        directory = new ByteBuffersDirectory();
    }

    @AfterEach
    void closeDirectory() throws IOException {
        directory.close();
    }

    @Test
    @DisplayName("opening empty read-only index throws")
    void testEmptyReadOnly() {
        BKDTree.Builder<TestModel> builder = new BKDTree.Builder<TestModel>(directory)
                .setReadOnly(true);
        assertThatThrownBy(builder::build).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("open empty read-write mode")
    void testEmpty() throws IOException {
        try (BKDTree<TestModel> index = new BKDTree.Builder<TestModel>(directory)
                .setReadOnly(false)
                .build()) {
        }
    }

    @Test
    @DisplayName("opening empty in append mode throws")
    void testEmptyAppend() {
        BKDTree.Builder<TestModel> builder = new BKDTree.Builder<TestModel>(directory)
                .setReadOnly(false)
                .setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        assertThatThrownBy(builder::build).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("opening multiple writer throws")
    void testAlwaysOneWriter() throws IOException {
        BKDTree.Builder<TestModel> builder = new BKDTree.Builder<TestModel>(directory)
                .setReadOnly(false);
        try (BKDTree<TestModel> index = new BKDTree.Builder<TestModel>(directory)
                .setReadOnly(false)
                .build()) {
            assertThatThrownBy(builder::build).isInstanceOf(IOException.class);
        }
    }

    @DisplayName("after index opened")
    @Nested
    class AfterIndexOpenedTests {
        BKDTree<TestModel> index;
        IndexSearcher searcher;

        @BeforeEach
        void openIndex() throws IOException {
            index = new BKDTree.Builder<TestModel>(directory)
                    .setReadOnly(false)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE)
                    .setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
                    .build();
            IndexReader reader = DirectoryReader.open(directory);
            try {
                searcher = new IndexSearcher(reader);
            } catch (Throwable t) {
                reader.close();
            }
        }

        @Test
        @DisplayName("test insert")
        void testInsert() throws IOException {
            List<TestModel> sourceData = TestModel.getSourceData();
            BkdTreeTestHelper.insertData(sourceData, index);
            long pointCount = sourceData.stream().filter(s -> s.id().equals("point")).count();
            TermQuery query = new TermQuery(new Term(BKDTree.ID_FIELD_NAME, "point"));
            assertThat(searcher.count(query)).isEqualTo(0);
            index.commit();
            assertThat(searcher.count(query)).isEqualTo(pointCount);
        }

        @AfterEach
        void closeIndex() throws IOException {
            IOUtils.close(index, searcher.getIndexReader());
        }
    }

}
