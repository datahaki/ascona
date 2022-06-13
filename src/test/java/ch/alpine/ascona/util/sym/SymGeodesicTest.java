// code by jph
package ch.alpine.ascona.util.sym;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.win.WindowFunctions;

class SymGeodesicTest {
  @Test
  void testSimple() {
    Scalar s1 = SymScalarLeaf.of(1);
    Scalar s2 = SymScalarLeaf.of(2);
    SymScalar s3 = new SymScalarPart(s1, s2, RationalScalar.HALF);
    Scalar scalar = new SymScalarPart(s1, s2, RationalScalar.of(1, 2));
    assertEquals(s3, scalar);
    Scalar evaluate = s3.evaluate();
    assertEquals(evaluate, RationalScalar.of(3, 2));
    TensorUnaryOperator tensorUnaryOperator = //
        GeodesicCenter.of(SymGeodesic.INSTANCE, WindowFunctions.DIRICHLET.get());
    Tensor vector = SymSequence.of(5);
    Tensor tensor = tensorUnaryOperator.apply(vector);
    assertEquals(tensor.toString(), "{{{0, 1, 1/2}, 2, 1/5}, {2, {3, 4, 1/2}, 4/5}, 1/2}");
    SymScalar symScalar = (SymScalar) tensor;
    SymLink root = symScalar.build();
    Tensor pose = root.getPosition();
    assertEquals(pose, Tensors.vector(2, -1.5));
    SymScalar res = (SymScalar) tensor;
    assertEquals(res.evaluate(), RealScalar.of(2));
  }
}
