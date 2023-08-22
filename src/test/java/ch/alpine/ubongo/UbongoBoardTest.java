// code by jph
package ch.alpine.ubongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class UbongoBoardTest {
  @Test
  void testSimple() {
    List<UbongoSolution> list = UbongoBoards.STANDARD.solve();
    assertEquals(list.size(), 13);
    // list.stream().mapToInt(s -> s.search()).forEach(System.out::println);
    // System.out.println(list);
    UbongoSolution ubongoSolution = list.get(0);
    // System.out.println(ubongoSolution.search());
  }

  @Test
  void testSimple2() {
    assertEquals(UbongoBoards.SHOTGUN1.solve().size(), 7);
  }

  @Test
  void testEleven() {
    for (UbongoBoards ubongoBoards : UbongoBoards.values())
      if (ubongoBoards.use() == 7) {
        // System.out.println(ubongoBoards);
        List<UbongoSolution> list = UbongoLoader.INSTANCE.load(ubongoBoards);
        // list.stream().mapToInt(s -> s.search()).forEach(System.out::println);
      }
  }

  @Test
  void testTwelve() {
    for (UbongoBoards ubongoBoards : UbongoBoards.values())
      if (ubongoBoards.use() == 12) {
        List<UbongoSolution> list = UbongoLoader.INSTANCE.load(ubongoBoards);
        // list.stream().mapToInt(s -> s.search()).forEach(System.out::println);
      }
  }
}
