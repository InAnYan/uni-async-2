package com.inanyan;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
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

    public Matrix multiply(Matrix matrix, ExecutorService executorService, Optional<Integer> limit) throws InterruptedException {
        if (this.getColsCount() != matrix.getRowsCount()) {
            throw new IllegalArgumentException("matrix has incorrect number of columns");
        }

        Semaphore semaphore = null;
        if (limit.isPresent()) {
            semaphore = new Semaphore(limit.get());
        }

        AtomicInteger indexCounter = new AtomicInteger(0);
        Matrix result = new Matrix(this.getRowsCount(), matrix.getColsCount());

        for (int c = 0; c < result.getElementsCount(); c++) {
            if (semaphore != null) {
                semaphore.acquire();
            }

            executorService.submit(() -> {
                int id = indexCounter.getAndIncrement();
                if (id >= result.getElementsCount()) {
                    executorService.shutdown();
                }

                int row = id / result.getRowsCount();
                int col = id % result.getRowsCount();

                float number = 0;
                for (int i = 0; i < this.getColsCount(); i++) {
                    number += this.get(row, i) * matrix.get(i, col);
                }

                result.set(row, col, number);
            });

            if (semaphore != null) {
                semaphore.release();
            }
        }

        return result;
    }

    public int getElementsCount() {
        return this.getRowsCount() * this.getColsCount();
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
