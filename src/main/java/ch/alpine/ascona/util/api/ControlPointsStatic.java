// code by jph
package ch.alpine.ascona.util.api;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public enum ControlPointsStatic {
  ;
  protected static final PointsRender POINTS_RENDER_1 = //
      new PointsRender(new Color(160, 160, 160, 128 + 64), Color.BLACK);

  public static void renderPoints( //
      ManifoldDisplay manifoldDisplay, Tensor points, GeometricLayer geometricLayer, Graphics2D graphics) {
    POINTS_RENDER_1.show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), points) //
        .render(geometricLayer, graphics);
  }
}
