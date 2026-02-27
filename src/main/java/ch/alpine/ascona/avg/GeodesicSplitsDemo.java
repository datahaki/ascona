// code by jph
package ch.alpine.ascona.avg;

import ch.alpine.ascony.sym.SymGeodesic;
import ch.alpine.ascony.sym.SymScalar;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophis.flt.ga.GeodesicExtrapolation;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GeodesicSplitsDemo extends AbstractSplitsDemo {
  @ReflectionMarker
  static class Param {
    public WindowFunctions kernel = WindowFunctions.DIRICHLET;
    public Boolean prediction = false;
  }

  private final Param param;

  public GeodesicSplitsDemo() {
    super(new SaveParam(), param = new Param());
  }

  @Override
  SymScalar symScalar(Tensor vector) {
    if (param.prediction)
      return 0 < vector.length() //
          ? (SymScalar) GeodesicExtrapolation.of(SymGeodesic.INSTANCE, param.kernel.get()).apply(vector)
          : null;
    if (Integers.isOdd(vector.length()))
      return (SymScalar) GeodesicCenter.of(SymGeodesic.INSTANCE, param.kernel.get()).apply(vector);
    return null;
  }

  static void main() {
    new GeodesicSplitsDemo().runStandalone();
  }
}
