// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Optional;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.sophus.hs.hn.HnWeierstrassCoordinate;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

public class H2Display extends HnDisplay implements HsArrayPlot {
  public static final ManifoldDisplay INSTANCE = new H2Display();

  // ---
  private H2Display() {
    super(2);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return p.extract(0, 2);
  }

  @Override // from HsArrayPlot
  public Optional<Tensor> raster(Tensor pxy) {
    return Optional.of(HnWeierstrassCoordinate.toPoint(pxy));
  }

  @Override
  public CoordinateBoundingBox coordinateBoundingBox() {
    return Box2D.xy(CLIP);
  }

  @Override
  public RenderInterface background() {
    return H2Background.INSTANCE;
  }
}
