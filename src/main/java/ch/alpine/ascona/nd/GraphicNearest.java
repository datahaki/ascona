// code by jph
package ch.alpine.ascona.nd;

import java.awt.Graphics2D;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.NdCenterInterface;
import ch.alpine.tensor.opt.nd.NdCollectNearest;
import ch.alpine.tensor.opt.nd.NdEntry;

/* package */ class GraphicNearest<V> extends NdCollectNearest<V> {
  private final GeometricLayer geometricLayer;
  private final Graphics2D graphics;

  protected GraphicNearest( //
      NdCenterInterface ndCenterInterface, int limit, //
      GeometricLayer geometricLayer, Graphics2D graphics) {
    super(ndCenterInterface, limit);
    this.geometricLayer = geometricLayer;
    this.graphics = graphics;
  }

  @Override // from NdVisitor
  public boolean isViable(CoordinateBoundingBox coordinateBoundingBox) {
    StaticHelper.draw(coordinateBoundingBox, geometricLayer, graphics);
    return super.isViable(coordinateBoundingBox);
  }

  @Override // from NdVisitor
  public void consider(NdEntry<V> ndEntry) {
    StaticHelper.draw(ndEntry.location(), geometricLayer, graphics);
    super.consider(ndEntry);
  }
}
