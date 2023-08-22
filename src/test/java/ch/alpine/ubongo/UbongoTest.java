// code by jph
package ch.alpine.ubongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UbongoTest {
  @Test
  void testSimple() {
    assertEquals(UbongoPiece.values().length, 12);
    assertEquals(UbongoPiece.C2.count(), 5);
  }

  @Test
  void testStampsSpec() {
    assertEquals(UbongoPiece.A0.variationCount(), 2);
    assertEquals(UbongoPiece.A1.variationCount(), 4);
    assertEquals(UbongoPiece.A2.variationCount(), 1);
    assertEquals(UbongoPiece.B1.variationCount(), 8);
    assertEquals(UbongoPiece.B2.variationCount(), 4);
    assertEquals(UbongoPiece.C0.variationCount(), 2);
    assertEquals(UbongoPiece.C2.variationCount(), 8);
  }

  @Test
  @Disabled
  void testStamps() {
    for (UbongoPiece ubongo : UbongoPiece.values())
      System.out.println(ubongo + " " + ubongo.stamps().size());
  }
}
