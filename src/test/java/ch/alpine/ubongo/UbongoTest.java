// code by jph
package ch.alpine.ubongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UbongoTest {
  @Test
  void testSimple() {
    assertEquals(Ubongo.values().length, 12);
    assertEquals(Ubongo.C2.count(), 5);
  }

  @Test
  void testStampsSpec() {
    assertEquals(Ubongo.A0.variationCount(), 2);
    assertEquals(Ubongo.A1.variationCount(), 4);
    assertEquals(Ubongo.A2.variationCount(), 1);
    assertEquals(Ubongo.B1.variationCount(), 8);
    assertEquals(Ubongo.B2.variationCount(), 4);
    assertEquals(Ubongo.C0.variationCount(), 2);
    assertEquals(Ubongo.C2.variationCount(), 8);
  }

  @Test
  @Disabled
  void testStamps() {
    for (Ubongo ubongo : Ubongo.values())
      System.out.println(ubongo + " " + ubongo.stamps().size());
  }
}
