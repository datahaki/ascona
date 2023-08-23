package ch.alpine.ubongo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class UbongoSolutionTest {
  @Test
  void test() {
    UbongoSolution ubongoSolution = new UbongoSolution(List.of(), 17000);
    double log10 = Math.log10(ubongoSolution.search());
    assertTrue(log10 < 5);
  }
}
