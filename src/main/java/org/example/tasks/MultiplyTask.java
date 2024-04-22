package org.example.tasks;

import org.example.cli.MainCLI;
import org.example.model.Matrix;

import java.math.BigInteger;
import java.util.concurrent.RecursiveTask;

public class MultiplyTask extends RecursiveTask implements Task {
    private Matrix A;
    private Matrix B;
    private Matrix R;
    private int start;
    private int end;

    public MultiplyTask(Matrix a, Matrix b) {
        this.A = a;
        this.B = b;
        this.start = 0;
        this.end = A.getRows();
        this.R = new Matrix(A.getName() + B.getName(), A.getRows(), B.getCols());

    }

    public MultiplyTask(Matrix a, Matrix b, Matrix r, int start, int end) {
        A = a;
        B = b;
        R = r;
        this.start = start;
        this.end = end;
    }

    @Override
    public TaskType getType() {
        return TaskType.MULTIPLY;
    }

    @Override
    protected Object compute() {
        //todo config
        if (end - start <= MainCLI.MAX_ROWS) {
            multiplyMatrices(start, end);
        } else {
            int mid = start + ((end - start) / 2);

            MultiplyTask left = new MultiplyTask(A, B, R, start, mid);
            MultiplyTask right = new MultiplyTask(A, B, R, mid, end);

            left.fork();
            right.compute();
            left.join();
        }
        return R;
    }

    private void multiplyMatrices(int start, int end) {
        for (int i = start; i < end; i++) {
            for (int j = 0; j < B.getCols(); j++) {
                R.setValue(i, j, multiplyRowColumn(i, j));
            }
        }
    }

    private BigInteger multiplyRowColumn(int matrix1Row, int matrix2Column) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < A.getCols(); i++) {
            result = result.add(A.getValue(matrix1Row, i).multiply(B.getValue(i, matrix2Column)));
        }
        return result;
    }

    public Matrix getA() {
        return A;
    }

    public Matrix getB() {
        return B;
    }


}
