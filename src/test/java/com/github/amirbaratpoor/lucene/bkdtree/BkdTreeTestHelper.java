package com.github.amirbaratpoor.lucene.bkdtree;

import com.github.amirbaratpoor.lucene.TestModel;

import java.io.IOException;
import java.util.List;

final class BkdTreeTestHelper {

    public static void insertData(List<TestModel> data, BKDTree<TestModel> index) throws IOException {
        for (TestModel datum : data) {
            index.insert(datum.id(), datum.geometry(), datum);
        }
    }
}
