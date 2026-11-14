// code by jph
package ch.alpine.ascona.geo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SingleTaskController {
  public static void main(String[] args) {
    // 1. Create a single-thread executor (Enforces 1 task at a time)
    ExecutorService executor = Executors.newSingleThreadExecutor();
    // Keep track of the currently running task's Future
    final Future<?>[] activeTaskHolder = new Future[1];
    // --- Step A: Submit Task 1 (A long-running task) ---
    System.out.println("Submitting Task 1...");
    activeTaskHolder[0] = executor.submit(() -> {
      try {
        System.out.println("Task 1 started working...");
        for (int i = 1; i <= 10; i++) {
          // Check for cancellation or interruption
          if (Thread.currentThread().isInterrupted()) {
            System.out.println("Task 1 was interrupted mid-way!");
            return;
          }
          Thread.sleep(500); // Simulating work step
          System.out.println("Task 1 progress: step " + i);
        }
        System.out.println("Task 1 finished normally.");
      } catch (InterruptedException e) {
        // Thread.sleep throws InterruptedException and clears the flag
        System.out.println("Task 1 was prematurely terminated!");
        Thread.currentThread().interrupt(); // Restore interrupt status
      }
    });
    // Let Task 1 run for a little bit
    sleep(1200);
    // --- Step B: Prematurely terminate Task 1 to run Task 2 ---
    System.out.println("\n--- User requests to cancel Task 1 and run Task 2 ---");
    if (activeTaskHolder[0] != null && !activeTaskHolder[0].isDone()) {
      boolean cancelled = activeTaskHolder[0].cancel(true); // Interrupts the t
      System.out.println("Task 1 cancellation signal sent: " + cancelled);
    }
    // --- Step C: Submit Task 2 immediately after ---
    activeTaskHolder[0] = executor.submit(() -> {
      System.out.println("Task 2 started immediately!");
      try {
        Thread.sleep(500);
        System.out.println("Task 2 completed successfully.");
      } catch (InterruptedException e) {
        System.out.println("Task 2 interrupted.");
      }
    });
    // Keep main alive to watch output
    sleep(1500);
    executor.shutdownNow();
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
