// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.Tensor;

public class R3Display extends RnDisplay {
  public static final ManifoldDisplay INSTANCE = new R3Display();

  private R3Display() {
    super(3);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(p);
  }
}
