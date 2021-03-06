// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.lie.he.HeGroup;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dot;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Chop;

class He1DisplayTest {
  @Test
  void testSimple() {
    assertEquals(He1Display.INSTANCE.geodesicSpace(), HeGroup.INSTANCE);
  }

  @Test
  void testWhatever() {
    Distribution distribution = NormalDistribution.standard();
    Tensor a = RandomVariate.of(distribution, 3, 3);
    Tensor v = RandomVariate.of(distribution, 3);
    Tensor b = DiagonalMatrix.with(v);
    Tensor c = RandomVariate.of(distribution, 3, 3);
    Chop._09.requireClose(Dot.of(b, c), Times.of(v, c));
    Chop._09.requireClose(Dot.of(a, b, c), a.dot(Times.of(v, c)));
  }
}
