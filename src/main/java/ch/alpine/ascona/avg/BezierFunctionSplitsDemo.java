// code by jph
package ch.alpine.ascona.avg;

import ch.alpine.ascony.sym.SymGeodesic;
import ch.alpine.ascony.sym.SymScalar;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.BezierFunction;

/** visualization of geodesic average along geodesics */
class BezierFunctionSplitsDemo extends AbstractSplitsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar ratio = RealScalar.of(0.5);
  }

  private final Param0 param0;

  public BezierFunctionSplitsDemo() {
    super(new SaveParam(), param0 = new Param0());
  }

  @Override // from GeodesicAverageDemo
  SymScalar symScalar(Tensor vector) {
    int n = vector.length();
    if (0 < n) {
      ScalarTensorFunction scalarTensorFunction = new BezierFunction(SymGeodesic.INSTANCE, vector);
      Scalar parameter = n <= 1 //
          ? RealScalar.ZERO
          : Rational.of(n, n - 1);
      parameter = parameter.multiply(param0.ratio);
      return (SymScalar) scalarTensorFunction.apply(parameter);
    }
    return null;
  }

  static void main() {
    new BezierFunctionSplitsDemo().runStandalone();
  }
}
