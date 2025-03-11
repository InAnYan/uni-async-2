package com.inanyan;

import java.util.concurrent.locks.ReentrantLock;

public class MatrixMultiplierThread implements Runnable{
    private final Matrix a;
    private final Matrix b;
    private final Matrix c;
    private final ReentrantLock[][] locks;

    private int index = 0;

    public MatrixMultiplierThread(Matrix a, Matrix b, Matrix c, ReentrantLock[][] locks) {
        if (a.getColsCount() != b.getRowsCount()) {
            throw new IllegalArgumentException("wrong matrix multiplication dimensions");
        }

        if (locks.length != a.getRowsCount() || locks.length == 0 || locks[0].length != b.getColsCount()) {
            throw new IllegalArgumentException("lock matrix has wrong dimensions");
        }

        if (c.getRowsCount() != a.getRowsCount() || c.getColsCount() != b.getColsCount()) {
            throw new IllegalArgumentException("wrong matrix multiplication dimensions for result matrix");
        }

        this.a = a;
        this.b = b;
        this.c = c;
        this.locks = locks;
    }

    @Override
    public void run() {
        while (index < getMaxIndex()) {
            ReentrantLock lock = locks[getCurrentRow()][getCurrentCol()];
            if (lock.tryLock()) {
                float result = performMultiplication();
                c.set(getCurrentRow(), getCurrentCol(), result);
            }

            index++;
        }
    }

    private float performMultiplication() {
        float result = 0;

        for (int i = 0; i < a.getColsCount(); i++) {
            result += a.get(getCurrentRow(), i) * b.get(i, getCurrentCol());
        }

        return result;
    }

    private int getCurrentRow() {
        return index / a.getColsCount();
    }

    private int getCurrentCol() {
        return index % a.getColsCount();
    }

    private int getMaxIndex() {
        return c.getColsCount() * c.getRowsCount();
    }
}
