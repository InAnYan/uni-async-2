package com.inanyan;

import org.openjdk.jmh.annotations.*;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
public class MyBenchmark {
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @State(Scope.Thread)
    public static class MyState {
        public Matrix a;
        public Matrix b;

        @Param({ "1000" })
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
    public Matrix simplePool(MyState state) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(state.threadCount);

        Matrix result = state.a.multiply(state.b, executorService, Optional.empty());

        executorService.shutdown();

        return result;
    }

    @Benchmark
    public Matrix virtualThreadsUnbound(MyState state) throws InterruptedException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        Matrix result = state.a.multiply(state.b, executorService, Optional.empty());

        executorService.shutdown();

        return result;
    }

    @Benchmark
    public Matrix virtualThreadsBound(MyState state) throws InterruptedException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        Matrix result = state.a.multiply(state.b, executorService, Optional.of(state.threadCount));

        executorService.shutdown();

        return result;
    }
}
/*
Benchmark                          (size)  (threadCount)  Mode  Cnt  Score   Error  Units
MyBenchmark.simplePool                 10              4  avgt   20  0.419 ± 0.080  ms/op
MyBenchmark.simplePool                 10             10  avgt   20  0.689 ± 0.134  ms/op
MyBenchmark.simplePool                100              4  avgt   20  2.431 ± 0.763  ms/op
MyBenchmark.simplePool                100             10  avgt   20  3.741 ± 1.356  ms/op
MyBenchmark.virtualThreadsBound        10              4  avgt   20  0.193 ± 0.059  ms/op
MyBenchmark.virtualThreadsBound        10             10  avgt   20  0.181 ± 0.063  ms/op
MyBenchmark.virtualThreadsBound       100              4  avgt   20  4.595 ± 1.600  ms/op
MyBenchmark.virtualThreadsBound       100             10  avgt   20  4.648 ± 1.078  ms/op
MyBenchmark.virtualThreadsUnbound      10              4  avgt   20  0.202 ± 0.169  ms/op
MyBenchmark.virtualThreadsUnbound      10             10  avgt   20  0.156 ± 0.044  ms/op
MyBenchmark.virtualThreadsUnbound     100              4  avgt   20  4.130 ± 1.588  ms/op
MyBenchmark.virtualThreadsUnbound     100             10  avgt   20  5.822 ± 5.889  ms/op
 */