// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.sophus.crv.TransitionSpace;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidBuilders;
import ch.alpine.sophus.crv.clt.ClothoidTransitionSpace;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.Tensor;

public final class Se2ClothoidDisplay extends AbstractClothoidDisplay {
  public static final ManifoldDisplay ANALYTIC = //
      new Se2ClothoidDisplay(ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder(), "ClA");
  public static final ManifoldDisplay LEGENDRE = //
      new Se2ClothoidDisplay(ClothoidBuilders.SE2_LEGENDRE.clothoidBuilder(), "Cl3");
  // ---
  private final ClothoidBuilder clothoidBuilder;
  private final String string;

  private Se2ClothoidDisplay(ClothoidBuilder clothoidBuilder, String string) {
    this.clothoidBuilder = clothoidBuilder;
    this.string = string;
  }

  @Override // from AbstractClothoidDisplay
  public ClothoidBuilder geodesicSpace() {
    return clothoidBuilder;
  }

  @Override // from ManifoldDisplay
  public TransitionSpace transitionSpace() {
    return ClothoidTransitionSpace.ANALYTIC;
  }

  @Override // from ManifoldDisplay
  public Tensor xya2point(Tensor xya) {
    Tensor xym = xya.copy();
    xym.set(So2.MOD, 2);
    return xym;
  }

  @Override // from Object
  public String toString() {
    return string;
  }
}
