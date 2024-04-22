package org.example.mulextr;

import java.util.concurrent.ForkJoinPool;

public class MatrixExtractor {
    private ForkJoinPool forkJoinPool;

    public MatrixExtractor() {
        this.forkJoinPool = new ForkJoinPool();
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

}
