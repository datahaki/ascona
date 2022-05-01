// code by jph
package ch.alpine.ascona.dis;

import org.junit.jupiter.api.Test;

class Se2ClothoidDisplayTest {
  @Test
  public void testSimple() {
    // 1 2.5180768787131558
    // 2 2.5597567801548426
    // 3 2.5640965868005288
    // 4 2.564420707620397
    // TODO
    // Tensor p = Tensors.vector(0, 0, 0);
    // Tensor q = Tensors.vector(0, 2, 0);
    // Scalar scalar = Se2ClothoidDisplay.ANALYTIC.parametricDistance().distance(p, q);
    // Clips.interval(2.542, 2.55).requireInside(scalar);
    // Scalar result = Se2Parametric.INSTANCE.distance(p, q);
    // assertEquals(result, RealScalar.of(2));
  }

  @Test
  public void testShapeArea() {
    // TODO
    // Scalar a1 = PolygonArea.of(Arrowhead.of(0.4));
    // Scalar a2 = PolygonArea.of(Se2ClothoidDisplay.ANALYTIC.shape());
    // Tolerance.CHOP.requireClose(a1, a2);
  }
}
