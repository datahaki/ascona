// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

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

  @Override
  public CoordinateBoundingBox coordinateBoundingBox() {
    return null;
  }

  @Override // from ManifoldDisplay
  public HsArrayPlot hsArrayPlot() {
    return null;
  }

  @Override
  public RenderInterface background() {
    return H1Background.INSTANCE;
  }
}
