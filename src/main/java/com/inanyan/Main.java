package com.inanyan;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Matrix a = new Matrix(new float[][] {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 }
        });

        Matrix b = new Matrix(new float[][] {
                { -1,  2, -3 },
                {  4, -5,  6 },
                { -7,  8, -9 }
        });

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        Matrix c = a.multiply(b, executorService, Optional.of(6));

        System.out.println(c);

        executorService.shutdown();
    }
}