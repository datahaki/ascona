// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Optional;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.sophus.hs.hn.HnWeierstrassCoordinate;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

public class H2Display extends HnDisplay implements D2Raster {
  public static final ManifoldDisplay INSTANCE = new H2Display();

  private H2Display() {
    super(2);
  }

  @Override // from HsArrayPlot
  public Optional<Tensor> d2lift(Tensor pxy) {
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
