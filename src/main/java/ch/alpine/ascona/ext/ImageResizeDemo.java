// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.img.ImageResize;

public class ImageResizeDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "5", max = "20")
    public Integer size = 10;
  }

  private final Param param;

  public ImageResizeDemo() {
    this(new Param());
  }

  public ImageResizeDemo(Param param) {
    super(param);
    this.param = param;
    bufferedImage = VehicleStatic.INSTANCE.bufferedImage_c();
  }

  private final BufferedImage bufferedImage;

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();
    int[] hints = { //
        Image.SCALE_DEFAULT, //
        Image.SCALE_FAST, //
        Image.SCALE_SMOOTH, // good quality
        Image.SCALE_REPLICATE, //
        Image.SCALE_AREA_AVERAGING // good quality
    };
    int count = 0;
    int size = param.size;
    for (int hint : hints) {
      Graphics2D g = (Graphics2D) graphics.create();
      g.scale(size * 0.1, size * 0.1);
      Image image = bufferedImage; // .getScaledInstance(width * size / 10, height * size / 10, hint);
      g.drawImage(image, 0, count * (height * size / 10), null);
      ++count;
    }
    graphics.drawImage( //
        ImageResize.of(bufferedImage, width * size / 10, height * size / 10, AffineTransformOp.TYPE_NEAREST_NEIGHBOR), 300, 0, null);
  }

  static void main() {
    launch();
  }
}
