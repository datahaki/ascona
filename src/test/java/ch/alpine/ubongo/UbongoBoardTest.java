// code by jph
package ch.alpine.ubongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.ext.Integers;

class UbongoBoardTest {
  @Test
  void testSimple() {
    List<UbongoSolution> list = UbongoBoards.STANDARD.solve();
    assertEquals(list.size(), 13);
    // list.stream().mapToInt(s -> s.search()).forEach(System.out::println);
    // System.out.println(list);
    UbongoSolution ubongoSolution = list.get(0);
    Integers.requireLessEquals(ubongoSolution.search(), 13);
  }

  @Test
  void testSimple2() {
    assertEquals(UbongoBoards.SHOTGUN1.solve().size(), 7);
  }

  @Test
  void testEleven() {
    UbongoBoards ubongoBoards = UbongoBoards.SIMSONSB;
    assertEquals(ubongoBoards.use(), 7);
    List<UbongoSolution> list = UbongoLoader.INSTANCE.load(ubongoBoards);
    assertEquals(list.size(), 9);
  }

  @Test
  void testTwelve() {
    UbongoBoards ubongoBoards = UbongoBoards.FINALBOS;
    assertEquals(ubongoBoards.use(), 12);
    List<UbongoSolution> list = UbongoLoader.INSTANCE.load(ubongoBoards);
    assertEquals(list.size(), 1);
    UbongoSolution ubongoSolution = list.get(0);
    assertEquals(ubongoSolution.list().size(), 12);
    Integers.requireLessEquals(ubongoSolution.search(), 9000);
  }
}
