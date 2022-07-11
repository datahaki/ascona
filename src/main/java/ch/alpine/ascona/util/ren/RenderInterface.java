// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.win.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;

/** capability for drawing in {@link GeometricComponent} */
@FunctionalInterface
public interface RenderInterface {
  /** @param geometricLayer to map model coordinates to pixel coordinates
   * @param graphics */
  void render(GeometricLayer geometricLayer, Graphics2D graphics);
}
