// code by jph
package ch.alpine.ascona.geo;

import java.util.concurrent.CompletableFuture;

class Pipeline1Demo {
  public static void main(String[] args) {
    System.out.println("Main thread starts: " + Thread.currentThread().getName());
    CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
      // Stage 1: Fetch User ID
      sleep(500);
      return "user_123";
    });
    String string = completableFuture.join();
    IO.println("join=" + string);
    completableFuture.thenApply(userId -> {
      // Stage 2: Fetch User Profile
      System.out.println("Fetching profile for: " + userId);
      sleep(500);
      return "Alice (Admin)";
    }).thenApply(profile -> {
      // Stage 3: Format Output
      return "WELCOME " + profile.toUpperCase();
    }).thenAccept(result -> {
      // Final Stage: Consume the result (terminal operation)});
      System.out.println("Result received:" + result);
    });
    // Keep main thread alive long enough to see async output
    sleep(2000);
    System.out.println("Main thread ends.");
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
