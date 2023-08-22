package ch.alpine.ubongo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensors;

class StaticHelperTest {
  @Test
  void test() {
    assertTrue(StaticHelper.isSingleFree(Tensors.fromString("{{-1,-1,0}}")));
    assertFalse(StaticHelper.isSingleFree(Tensors.fromString("{{-1,0,-1,0}}")));
  }
}
