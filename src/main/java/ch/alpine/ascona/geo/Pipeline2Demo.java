// code by jph
package ch.alpine.ascona.geo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class Pipeline2Demo {
  public static void main(String[] args) {
    System.out.println("Main thread starts: " + Thread.currentThread().getName());
    CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
      // Stage 1: Fetch User ID
      sleep(2000);
      return "user_123";
    }) //
        .orTimeout(1, TimeUnit.SECONDS) //
        .exceptionally(ex -> {
          return "no can do!";
        });
    // completableFuture.get();
    String join = completableFuture.join();
    IO.println(join);
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
