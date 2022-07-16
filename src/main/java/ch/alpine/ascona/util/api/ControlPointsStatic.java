// code by jph
package ch.alpine.ascona.util.api;

import java.awt.Color;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.tensor.Tensor;

public enum ControlPointsStatic {
  ;
  private static final PointsRender GRAY_POINTS = new PointsRender( //
      new Color(160, 160, 160, 128), //
      Color.BLACK);

  /** @param manifoldDisplay
   * @param points */
  public static RenderInterface gray(ManifoldDisplay manifoldDisplay, Tensor points) {
    return GRAY_POINTS.show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), points);
  }
}
