package org.example.mulextr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class MatrixMultiplier {
    private ForkJoinPool forkJoinPool;
    public MatrixMultiplier() {
        this.forkJoinPool = new ForkJoinPool();
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

}
