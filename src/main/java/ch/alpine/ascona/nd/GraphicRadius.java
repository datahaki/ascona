// code by jph
package ch.alpine.ascona.nd;

import java.awt.Graphics2D;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.NdCenterInterface;
import ch.alpine.tensor.opt.nd.NdCollectRadius;
import ch.alpine.tensor.opt.nd.NdEntry;

/* package */ class GraphicRadius<V> extends NdCollectRadius<V> {
  private final GeometricLayer geometricLayer;
  private final Graphics2D graphics;

  protected GraphicRadius( //
      NdCenterInterface ndCenterInterface, Scalar radius, //
      GeometricLayer geometricLayer, Graphics2D graphics) {
    super(ndCenterInterface, radius);
    this.geometricLayer = geometricLayer;
    this.graphics = graphics;
  }

  @Override
  public boolean isViable(CoordinateBoundingBox coordinateBoundingBox) {
    StaticHelper.draw(coordinateBoundingBox, geometricLayer, graphics);
    return super.isViable(coordinateBoundingBox);
  }

  @Override
  public void consider(NdEntry<V> ndEntry) {
    StaticHelper.draw(ndEntry.location(), geometricLayer, graphics);
    super.consider(ndEntry);
  }
}
