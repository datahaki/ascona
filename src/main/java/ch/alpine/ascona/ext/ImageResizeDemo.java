// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics2D;
import java.awt.Image;
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
    @FieldClip(min = "5", max = "30")
    public Integer size = 20;
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
    int size = param.size;
    Graphics2D g = (Graphics2D) graphics.create();
    Image image = bufferedImage;
    g.drawImage(image, 0, 0, null);
    final int piw = width * size / 10;
    final int pih = height * size / 10;
    int piy = height;
    for (ImageResize imageResize : ImageResize.values()) {
      graphics.drawImage(imageResize.of(bufferedImage, piw, pih), 0, piy, null);
      piy += pih;
    }
  }

  static void main() {
    launch();
  }
}
