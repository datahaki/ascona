// code by jph
package ch.alpine.ascona.util.sym;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.ext.Serialization;

class SymScalarTest {
  @Test
  public void testSimple() throws ClassNotFoundException, IOException {
    Serialization.copy(SymScalarLeaf.of(3)).hashCode();
    new SymScalarPart(SymScalarLeaf.of(2), SymScalarLeaf.of(3), RationalScalar.HALF).hashCode();
  }

  @Test
  public void testFail() {
    assertThrows(Exception.class, () -> new SymScalarPart(SymScalarLeaf.of(2), RealScalar.of(3), RationalScalar.HALF));
  }
}
