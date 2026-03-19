// code by jph
package ch.alpine.ascona.dat;

import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.tensor.Tensor;

public enum GokartPoseDatas {
  ;
  public static final Tensor HANGAR_MODEL2PIXEL = //
      PvmBuilder.rhs().setOffset(100, 800).setPerPixel(7.5).digest();
}
