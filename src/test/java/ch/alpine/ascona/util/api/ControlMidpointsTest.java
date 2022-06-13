// code by jph
package ch.alpine.ascona.util.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

class ControlMidpointsTest {
  @Test
  void testSimple() {
    CurveSubdivision curveSubdivision = new ControlMidpoints(RnGroup.INSTANCE);
    Tensor tensor = curveSubdivision.string(Tensors.vector(1, 2, 3));
    assertEquals(tensor.toString(), "{1, 3/2, 5/2, 3}");
  }
}
