// code by jph
package ch.alpine.ascona.ext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;
import java.util.Objects;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.ScalableImage;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;

public class ImageTransitionDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldClip(min = "0.01", max = "0.1")
    @FieldSlider
    public Scalar ex = RealScalar.of(0.05);
    @FieldClip(min = "0", max = "1")
    @FieldSlider
    public Scalar c1 = RealScalar.of(0.3);
  }

  private final Param param;
  private ScalableImage im1 = null;
  private ScalableImage im2 = null;

  public ImageTransitionDemo() {
    this(new Param());
  }

  public ImageTransitionDemo(Param param) {
    super(param);
    this.param = param;
    try {
      im1 = new ScalableImage(VehicleStatic.INSTANCE.bufferedImage_c(), AffineTransformOp.TYPE_BICUBIC);
      im2 = new ScalableImage(VehicleStatic.INSTANCE.bufferedImage_g(), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    } catch (Exception exception) {
      throw new RuntimeException();
    }
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (Objects.isNull(im1) || Objects.isNull(im2))
      return;
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    Rectangle rectangle = new Rectangle(100, 50, dimension.width - 200, dimension.height - 100);
    // ---
    graphics.drawImage(im2.getScaledInstance(rectangle.width, rectangle.height), rectangle.x, rectangle.y, null);
    int ext = (int) (rectangle.width * param.ex.number().floatValue());
    int x = (int) ((rectangle.width + 2 * ext) * param.c1.number().floatValue()) - ext;
    int _x = Math.max(0, x);
    graphics.setClip(rectangle.x + _x, rectangle.y, rectangle.width - _x, rectangle.height);
    graphics.drawImage(im1.getScaledInstance(rectangle.width, rectangle.height), rectangle.x, rectangle.y, null);
    graphics.setClip(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    Color color_lo = new Color(255, 255, 255, 0);
    Color color_hi = new Color(0, 0, 0, 192);
    Paint paint = new LinearGradientPaint(rectangle.x + x - ext, 0, rectangle.x + x + ext, 0, new float[] { 0f, 0.5f, 1f },
        new Color[] { color_lo, color_hi, color_lo });
    graphics.setPaint(paint);
    graphics.fillRect(rectangle.x + x - ext, rectangle.y, 2 * ext, rectangle.height);
  }

  static void main() {
    launch();
  }
}
