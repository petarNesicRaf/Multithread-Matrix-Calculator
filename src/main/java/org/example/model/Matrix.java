package org.example.model;

import java.io.File;
import java.math.BigInteger;

public class Matrix {
    private   String name;
    private  int rows;
    private  int cols;
    private  BigInteger[][] data;
    private File matrixFile;
    public Matrix(String name, int rows, int cols) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.data = new BigInteger[rows][cols];
        fillWithZeros();
    }
    private void fillWithZeros() {
        this.data = new BigInteger[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = BigInteger.ZERO;
            }
        }
    }

    public  void printMatrix() {
        System.out.println("Matrix: " + name);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(data[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public void setValue(int i, int j, BigInteger value){
        data[i][j] = value;
    }
    public BigInteger getValue(int i, int j){
        return data[i][j];
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }


    public int getCols() {
        return cols;
    }



    public File getMatrixFile() {
        return matrixFile;
    }

    public void setMatrixFile(File matrixFile) {
        this.matrixFile = matrixFile;
    }
}
