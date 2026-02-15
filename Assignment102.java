// Estimate the value of Pi using Monte-Carlo Method, using parallel program
package assignments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class Assignment102 {

  //Scalabilité Forte
public static void main(String[] args) {
    int N_TOTAL = 16000000;

    // Nombre max de threads disponibles sur la machine
    int nThreadsMax = Runtime.getRuntime().availableProcessors();
    System.out.println("Processeurs logiques disponibles : " + nThreadsMax);

    // nb de proces
    int p = 8; // Exemple : 1,2,4,8 ou nThreadsMax

    int nThrowsPerThread = N_TOTAL ; //N_TOTAL / p si on veut la forte

    PiMonteCarlo pi = new PiMonteCarlo(nThrowsPerThread, p);
    long startTime = System.currentTimeMillis();
    double value = pi.getPi();
    long stopTime = System.currentTimeMillis();

    long duration = stopTime - startTime;

    // Écriture automatique dans le CSV
    try (FileWriter fw = new FileWriter("assigmentFaible.csv", true)) {
        fw.write(N_TOTAL + "," + p + "," + duration + "\n");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    System.out.println("Threads: " + p + " | Pi approx: " + value + " | Durée: " + duration + " ms");
}
}




class PiMonteCarlo {
    AtomicInteger nAtomSuccess;
    int nThrows;
    double value;
    int nbThreads;

    class MonteCarlo implements Runnable {
        @Override
        public void run() {
            double x = Math.random();
            double y = Math.random();
            if (x * x + y * y <= 1)
                nAtomSuccess.incrementAndGet();
        }
    }

    // Pour scalabilité forte (int nThrowsPerThread, int nbThreads)
    public PiMonteCarlo(int nThrowsPerThread, int nbThreads) {
        this.nAtomSuccess = new AtomicInteger(0);
        this.nThrows = nThrowsPerThread;
        this.nbThreads = nbThreads;
    }

    public double getPi() {
        int nProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newWorkStealingPool(nProcessors);
        for (int i = 1; i <= nThrows; i++) {
            Runnable worker = new MonteCarlo();
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        value = 4.0 * nAtomSuccess.get() / nThrows;
        return value;
    }
}