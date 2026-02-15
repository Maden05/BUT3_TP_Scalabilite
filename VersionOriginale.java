package assignments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Approximates PI using the Monte Carlo method. Demonstrates
 * use of Callables, Futures, and thread pools.
 */
class PiOG {
    public static void main(String[] args) throws Exception {

        long total;
        int workers = 4;          // nombre de threads
        int N_TOTAL = 10000000;   // nombre total de points

        int totalCount = N_TOTAL / workers;
        total = new MasterOG().doRun(totalCount, workers);

        System.out.println("Total from Master = " + total);
    }
}

/**
 * Creates workers to run the Monte Carlo simulation
 * and aggregates the results.
 */
class MasterOG {
    public long doRun(int totalCount, int numWorkers)
            throws InterruptedException, ExecutionException {

        long startTime = System.nanoTime();

        // Create a collection of tasks
        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < numWorkers; ++i) {
            tasks.add(new WorkerOG(totalCount));
        }

        // Run them and receive a collection of Futures
        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);
        List<Future<Long>> results = exec.invokeAll(tasks);

        long total = 0;

        // Assemble the results
        for (Future<Long> f : results) {
            total += f.get(); // barrière implicite
        }

        double pi = 4.0 * total / totalCount / numWorkers;

        long stopTime = System.nanoTime();

        System.out.println("\nPi = " + pi);
        System.out.println("Error = " + (pi - Math.PI));
        System.out.println("Time (ns) = " + (stopTime - startTime));

        exec.shutdown();
        return total;
    }
}

/**
 * Task for running the Monte Carlo simulation.
 */
class WorkerOG implements Callable<Long> {
    private final int numIterations;

    public WorkerOG(int num) {
        this.numIterations = num;
    }

    @Override
    public Long call() {
        long circleCount = 0;
        Random prng = new Random();

        for (int j = 0; j < numIterations; j++) {
            double x = prng.nextDouble();
            double y = prng.nextDouble();

            if ((x * x + y * y) < 1)
                ++circleCount;
        }
        return circleCount;
    }
}
