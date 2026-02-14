package assignments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Approximates PI using the Monte Carlo method.  Demonstrates
 * use of Callables, Futures, and thread pools.
 */
public class Pi {
    public static void main(String[] args) throws Exception {

        long total;
        //int[] workersList = {1, 2, 4, 6, 8, 12};
        int w = 16;
        //int repetitions = 10;
        int N_TOTAL = 16000000;


        int totalCount = N_TOTAL;
        total = new Master().doRun(totalCount, w);
        System.out.println("total from Master = " + total);

    }
}

/**
 * Creates workers to run the Monte Carlo simulation
 * and aggregates the results.
 */
class Master {
    public long doRun(int totalCount, int numWorkers) throws InterruptedException, ExecutionException, IOException {

        long startTime = System.nanoTime();

        // Create a collection of tasks
        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < numWorkers; ++i) {
            tasks.add(new Worker(totalCount));
        }

        // Run them and receive a collection of Futures
        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);
        List<Future<Long>> results = exec.invokeAll(tasks);
        long total = 0;

        // Assemble the results.
        for (Future<Long> f : results) {
            // Call to get() is an implicit barrier.  This will block
            // until result from corresponding worker is ready.
            total += f.get();
        }
        double pi = 4.0 * total / totalCount / numWorkers;

        long stopTime = System.nanoTime();

        try (FileWriter fw = new FileWriter("results.csv", true)) {
            fw.write(
                    totalCount * numWorkers + "," +
                            numWorkers + "," +
                            (stopTime - startTime) + "\n"
            );
        }

        System.out.println("\nPi : " + pi);
        double errorPi = (pi - Math.PI);
        System.out.println("Error: " + errorPi + "\n");

        System.out.println("Ntot: " + totalCount * numWorkers);
        System.out.println("Available processors: " + numWorkers);
        System.out.println("Time Duration (ns): " + (stopTime - startTime) + "\n");

        System.out.println(errorPi + " " + totalCount * numWorkers + " " + numWorkers + " " + (stopTime - startTime));

        exec.shutdown();
        return total;
    }
}

/**
 * Task for running the Monte Carlo simulation.
 */
class Worker implements Callable<Long> {
    private final int numIterations;

    public Worker(int num) {
        this.numIterations = num;
    }

    @Override
    public Long call() {
        long circleCount = 0;
        Random prng = new Random();
        for (int j = 0; j < numIterations; j++) {
            double x = prng.nextDouble();
            double y = prng.nextDouble();
            if ((x * x + y * y) < 1) ++circleCount;
        }
        return circleCount;
    }
}
