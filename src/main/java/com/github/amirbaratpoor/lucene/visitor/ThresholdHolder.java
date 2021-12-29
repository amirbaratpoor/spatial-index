package com.github.amirbaratpoor.lucene.visitor;

import java.util.concurrent.atomic.AtomicInteger;

public interface ThresholdHolder {

    static ThresholdHolder createThreadSafe(final int threshold) {
        return new ThresholdHolder() {
            private final AtomicInteger hitCount = new AtomicInteger();

            @Override
            public int incrementAndGet() {
                return hitCount.incrementAndGet();
            }

            @Override
            public boolean isThresholdReached() {
                return hitCount.getAcquire() > threshold;
            }

            @Override
            public int getThreshold() {
                return threshold;
            }
        };
    }

    static ThresholdHolder createMutable(final int threshold) {
        return new ThresholdHolder() {
            private int hitCount = 0;

            @Override
            public int incrementAndGet() {
                return ++hitCount;
            }

            @Override
            public boolean isThresholdReached() {
                return hitCount > threshold;
            }

            @Override
            public int getThreshold() {
                return threshold;
            }
        };
    }

    int incrementAndGet();

    boolean isThresholdReached();

    int getThreshold();
}
