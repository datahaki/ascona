// code by jph
package ch.alpine.ascona.avg;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.sophus.flt.ga.GeodesicExtrapolation;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.sca.win.WindowFunctions;

public class GeodesicSplitsDemo extends AbstractSplitsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.ALL);
    }

    public WindowFunctions kernel = WindowFunctions.DIRICHLET;
    public Boolean prediction = false;
  }

  private final Param param;

  public GeodesicSplitsDemo() {
    this(new Param());
  }

  public GeodesicSplitsDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {2, 2, 1}, {5, 0, 2}}"));
  }

  @Override
  SymScalar symScalar(Tensor vector) {
    if (param.prediction)
      return 0 < vector.length() //
          ? (SymScalar) GeodesicExtrapolation.of(SymGeodesic.INSTANCE, param.kernel.get()).apply(vector)
          : null;
    if (!Integers.isEven(vector.length()))
      return (SymScalar) GeodesicCenter.of(SymGeodesic.INSTANCE, param.kernel.get()).apply(vector);
    return null;
  }

  public static void main(String[] args) {
    launch();
  }
}
