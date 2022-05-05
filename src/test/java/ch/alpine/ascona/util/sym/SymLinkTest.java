// code by jph
package ch.alpine.ascona.util.sym;

import org.junit.jupiter.api.Test;

class SymLinkTest {
  @Test
  public void testNodeNull() {
    new SymLink(null, null, null);
  }
}
