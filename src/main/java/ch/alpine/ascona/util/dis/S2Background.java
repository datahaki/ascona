// code by jph
package ch.alpine.ascona.util.dis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.CirclePoints;

/* package */ enum S2Background implements RenderInterface {
  INSTANCE;

  private static final Tensor CIRCLE = CirclePoints.of(61);
  private static final Color CENTER = new Color(255, 255, 255, 128);
  private static final Color BORDER = new Color(192, 192, 192, 128);

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Point2D center = geometricLayer.toPoint2D(0, 0);
    float fradius = geometricLayer.model2pixelWidth(RealScalar.ONE);
    float[] dist = { 0.0f, 0.70f, 1.0f };
    Color[] colors = { CENTER, new Color(224, 224, 224, 128), BORDER };
    Paint paint = new RadialGradientPaint(center, fradius, dist, colors);
    graphics.setPaint(paint);
    graphics.fill(geometricLayer.toPath2D(CIRCLE));
  }
}
