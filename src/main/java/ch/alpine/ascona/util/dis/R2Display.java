// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Optional;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.VectorQ;

public class R2Display extends RnDisplay implements HsArrayPlot {
  public static final ManifoldDisplay INSTANCE = new R2Display();

  private R2Display() {
    super(2);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return VectorQ.requireLength(p, 2);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(p);
  }

  @Override // from GeodesicArrayPlot
  public Optional<Tensor> raster(Tensor pxy) {
    return Optional.of(pxy);
  }
}
