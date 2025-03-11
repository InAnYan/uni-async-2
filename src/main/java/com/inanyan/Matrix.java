package com.inanyan;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class Matrix {
    public float[][] array;

    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("rows and cols must be greater than 0");
        }

        this.array = new float[rows][cols];
    }

    public Matrix(int size) {
        this(size, size);
    }

    public static Matrix makeRandom(int rows, int cols) {
        Matrix matrix = new Matrix(rows, cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.set(i, j, (float) Math.random());
            }
        }

        return matrix;
    }

    public static Matrix makeRandom(int size) {
        return Matrix.makeRandom(size, size);
    }

    public Matrix(float[][] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("array is null or empty");
        } else if (array[0].length == 0) {
            throw new IllegalArgumentException("array has empty rows");
        } else {
            int cols = array[0].length;

            for (int i = 1; i < cols; i++) {
                if (array[i].length != cols) {
                    throw new IllegalArgumentException("array has incorrect length of rows");
                }
            }
        }

        this.array = array;
    }

    public int getRowsCount() {
        return array.length;
    }

    public int getColsCount() {
        // It is ensured in constructor that {@link array} is not empty.
        // If it also ensured that every sub-array of {@link array} has the same length.
        return array[0].length;
    }

    public float get(int row, int col) {
        if (row >= getRowsCount() && col >= getColsCount()) {
            throw new IllegalArgumentException("invalid index");
        }

        return array[row][col];
    }

    public void set(int row, int col, float value) {
        if (row >= getRowsCount() && col >= getColsCount()) {
            throw new IllegalArgumentException("invalid index");
        }

        array[row][col] = value;
    }

    public Matrix multiply(Matrix matrix, ExecutorService executorService, int poolSize) throws ExecutionException, InterruptedException {
        if (this.getColsCount() != matrix.getRowsCount()) {
            throw new IllegalArgumentException("matrix has incorrect number of columns");
        }

        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be greater than 0");
        }

        Matrix result = new Matrix(this.getRowsCount(), matrix.getColsCount());
        ReentrantLock[][] locks = new ReentrantLock[this.getRowsCount()][matrix.getColsCount()];
        for (int i = 0; i < this.getRowsCount(); i++) {
            for (int j = 0; j < matrix.getColsCount(); j++) {
                locks[i][j] = new ReentrantLock();
            }
        }

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < poolSize; i++) {
            MatrixMultiplierThread thread = new MatrixMultiplierThread(this, matrix, result, locks);
            Future<?> future = executorService.submit(thread);
            futures.add(future);
        }

        for (Future<?> future : futures) {
            future.get();
        }

        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        int width = getMaxNumberWidth();

        for (float[] row : this.array) {
            sb.append("| ");

            for (float v : row) {
                sb.append(String.format("%" + width + "f", v)).append(" ");
            }

            sb.append("|");

            sb.append("\n");
        }

        return sb.toString();
    }

    private int getMaxNumberWidth() {
        float max = Integer.MIN_VALUE;

        for (float[] row : this.array) {
            for (float num : row) {
                max = Math.max(max, num);
            }
        }

        return Float.toString(max).length();
    }
}