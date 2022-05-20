// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.tensor.Tensor;

public class H1Display extends HnDisplay {
  public static final ManifoldDisplay INSTANCE = new H1Display();

  // ---
  private H1Display() {
    super(1);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return p.copy();
  }

  @Override // from ManifoldDisplay
  public HsArrayPlot arrayPlot() {
    return null;
  }

  @Override
  public RenderInterface background() {
    return H1Background.INSTANCE;
  }
}
