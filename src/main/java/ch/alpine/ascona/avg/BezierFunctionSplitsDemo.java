// code by jph
package ch.alpine.ascona.avg;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.crv.BezierFunction;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarTensorFunction;

/** visualization of geodesic average along geodesics */
public class BezierFunctionSplitsDemo extends AbstractSplitsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.ALL);
    }

    @FieldClip(min = "0", max = "1")
    public Scalar ratio = RealScalar.of(0.5);
  }

  private final Param param;

  public BezierFunctionSplitsDemo() {
    this(new Param());
  }

  public BezierFunctionSplitsDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {2, 2, 1}, {5, 0, 2}}"));
    // ---
    setManifoldDisplay(ManifoldDisplays.S2);
    setControlPointsSe2(Tensors.fromString("{}"));
  }

  @Override // from GeodesicAverageDemo
  SymScalar symScalar(Tensor vector) {
    int n = vector.length();
    if (0 < n) {
      ScalarTensorFunction scalarTensorFunction = new BezierFunction(SymGeodesic.INSTANCE, vector);
      Scalar parameter = n <= 1 //
          ? RealScalar.ZERO
          : RationalScalar.of(n, n - 1);
      parameter = parameter.multiply(param.ratio);
      return (SymScalar) scalarTensorFunction.apply(parameter);
    }
    return null;
  }

  public static void main(String[] args) {
    launch();
  }
}
