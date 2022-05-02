// code by jph
package ch.alpine.java.ren;

import java.awt.Graphics2D;

import ch.alpine.bridge.gfx.GeometricLayer;

public enum EmptyRender implements RenderInterface {
  INSTANCE;

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    // ---
  }
}
