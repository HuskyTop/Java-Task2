import java.util.*;
import java.util.concurrent.*;

public class Task2 {

    private static final List<String> sharedResultList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // User Input
        System.out.println("=== Configuration ===");

        System.out.print("Enter range MAX NATURAL value [0, 1000]: ");
        int maxRange = getValidInt(scanner);

        // Validate range order
        if (Math.abs(maxRange) > 1000) {
            System.out.println("Warning: |MAX| > 1000. Setting up to limit.");
            maxRange = 1000;
        } else if (Math.abs(maxRange) <= 1) {
            System.out.println("Warning: |MAX| < 2. Setting to prime numbers minimum");
            maxRange = 2;
        } else if (maxRange < -1) {
            System.out.println("Warning: MAX < -1. Make abs to value");
            maxRange = Math.abs(maxRange);
        }

        // Data Generation
        // Generate random array size between 40 and 60
        int arraySize = 40 + random.nextInt(21);
        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < arraySize; i++) {
            int num = random.nextInt(maxRange + 1);
            numbers.add(num);
        }

        System.out.println("\nGenerated array size: " + arraySize + ". From 0 to " + maxRange);
        System.out.println("Data: " + numbers);

        // Thread Pool Setup
        int chunkSize = 10; // Sub-array size per thread
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<List<String>>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Task Submission
        // Split array into chunks and submit Callable tasks
        for (int i = 0; i < arraySize; i += chunkSize) {
            int end = Math.min(i + chunkSize, arraySize);
            List<Integer> chunk = numbers.subList(i, end);

            Callable<List<String>> task = new PrimeTask(chunk);
            Future<List<String>> future = executor.submit(task);
            futures.add(future);
        }

        System.out.println("Tasks submitted. Processing...");

        // Result Collection
        for (Future<List<String>> future : futures) {
            try {
                // Requirement: Implement logic using isDone()
                while (!future.isDone()) {
                    // Poll for completion (simulated wait)
                    Thread.sleep(10);
                }

                // Requirement: Check isCancelled()
                if (future.isCancelled()) {
                    System.out.println("Task was cancelled.");
                } else {
                    // Retrieve result and add to shared list
                    List<String> result = future.get();
                    sharedResultList.addAll(result);
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing task: " + e.getMessage());
            }
        }

        // Shutdown and Summary
        executor.shutdown();
        long endTime = System.currentTimeMillis();

        System.out.println("\n=== Processing Results ===");
        // Print all results
        for (String line : sharedResultList) {
            System.out.println(line);
        }

        System.out.println("\n------------------------------------------------");
        System.out.println("Total execution time: " + (endTime - startTime) + " ms");
    }

    // Helper: Validates integer input
    private static int getValidInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter an integer:");
            scanner.next();
        }
        return scanner.nextInt();
    }

    // Inner Callable Task
    static class PrimeTask implements Callable<List<String>> {
        private final List<Integer> chunk;

        public PrimeTask(List<Integer> chunk) {
            this.chunk = chunk;
        }

        @Override
        public List<String> call() {
            List<String> results = new ArrayList<>();
            String threadName = Thread.currentThread().getName();

            for (Integer N : chunk) {
                // Find primes up to N
                List<Integer> primes = findPrimes(N);

                String log = String.format("Thread [%s]: N=%d -> Primes: %s",
                        threadName, N, primes.toString());
                results.add(log);
            }
            return results;
        }

        // Logic: Find all prime numbers up to limit
        private List<Integer> findPrimes(int limit) {
            List<Integer> primes = new ArrayList<>();
            if (limit < 2)
                return primes;

            for (int i = 2; i <= limit; i++) {
                if (isPrime(i)) {
                    primes.add(i);
                }
            }
            return primes;
        }

        // Logic: Check if a single number is prime
        private boolean isPrime(int num) {
            if (num <= 1)
                return false;
            for (int i = 2; i <= Math.sqrt(num); i++) {
                if (num % i == 0)
                    return false;
            }
            return true;
        }
    }
}