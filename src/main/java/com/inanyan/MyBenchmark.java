package com.inanyan;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 2, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 2, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
public class MyBenchmark {
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @State(Scope.Thread)
    public static class MyState {
        public Matrix a;
        public Matrix b;

        @Param({ "10", "100" })
        public int size;

        @Param({ "4", "10" })
        public int threadCount;

        @Setup(Level.Trial)
        public void setup() {
            a = Matrix.makeRandom(size);
            b = Matrix.makeRandom(size);
        }
    }

    @Benchmark
    public Matrix simplePool(MyState state) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(state.threadCount);

        Matrix result = state.a.multiply(state.b, executorService, state.threadCount);

        executorService.shutdown();

        return result;
    }

    @Benchmark
    public Matrix virtualThreadsUnbound(MyState state) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        Matrix result = state.a.multiply(state.b, executorService, state.size * state.size);

        executorService.shutdown();

        return result;
    }

    @Benchmark
    public Matrix virtualThreadsBound(MyState state) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        Matrix result = state.a.multiply(state.b, executorService, state.size * state.size);

        executorService.shutdown();

        return result;
    }
}
