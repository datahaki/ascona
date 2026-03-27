// code by jph
package ch.alpine.ascona;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.pro.RunProviderNamings;

class CheckNames {
  @Test
  void testSimple() throws Exception {
    assertTrue(RunProviderNamings.of(getClass().getPackageName()));
  }
}
