// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.arp.H2ArrayPlot;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

public class H2Display extends HnDisplay {
  public static final ManifoldDisplay INSTANCE = new H2Display();

  // ---
  private H2Display() {
    super(2);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return p.extract(0, 2);
  }

  @Override
  public CoordinateBoundingBox coordinateBoundingBox() {
    return Box2D.xy(Clips.absolute(RADIUS));
  }

  @Override // from ManifoldDisplay
  public HsArrayPlot arrayPlot() {
    return new H2ArrayPlot(coordinateBoundingBox());
  }

  @Override
  public RenderInterface background() {
    return H2Background.INSTANCE;
  }
}
