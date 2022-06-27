// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;

class S2DisplayTest {
  @Test
  void testSimple() {
    Tensor tensor = S2Display.tangentSpace(Tensors.vector(0, 1, 0));
    assertEquals(Dimensions.of(tensor), List.of(2, 3));
  }

  @Test
  void testInvariant() {
    ManifoldDisplay manifoldDisplay = S2Display.INSTANCE;
    Tensor xyz = manifoldDisplay.xya2point(Tensors.vector(1, 2, 0));
    Tensor xy = manifoldDisplay.point2xy(xyz);
    Tolerance.CHOP.requireClose(Vector2Norm.of(xy), RealScalar.ONE);
  }

  @Test
  void testTangent() {
    Tensor xyz = Vector2Norm.NORMALIZE.apply(Tensors.vector(1, 0.3, 0.5));
    Tensor matrix = S2Display.tangentSpace(xyz);
    assertEquals(Dimensions.of(matrix), List.of(2, 3));
    Tolerance.CHOP.requireAllZero(matrix.dot(xyz));
  }

  @Test
  void testProjTangent() {
    S2Display s2GeodesicDisplay = (S2Display) S2Display.INSTANCE;
    for (int index = 0; index < 10; ++index) {
      Tensor xya = RandomVariate.of(NormalDistribution.standard(), 3);
      Tensor xyz = s2GeodesicDisplay.xya2point(xya);
      Tensor tan = s2GeodesicDisplay.createTangent(xya);
      Tolerance.CHOP.requireAllZero(xyz.dot(tan));
    }
  }

  @Test
  void testFail() {
    assertThrows(Exception.class, () -> S2Display.tangentSpace(Tensors.vector(1, 1, 1)));
  }
}
