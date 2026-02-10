// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.ScalableImage;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.img.ImageResize;

public class ImageResizeDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "0.1", max = "5")
    public Scalar magnify = RealScalar.of(1);
  }

  private final Param param;

  public ImageResizeDemo() {
    this(new Param());
  }

  private final Map<ImageResize, ScalableImage> map = new EnumMap<>(ImageResize.class);

  public ImageResizeDemo(Param param) {
    super(param);
    this.param = param;
    BufferedImage bufferedImage = VehicleStatic.INSTANCE.bufferedImage_c();
    for (ImageResize imageResize : ImageResize.values())
      map.put(imageResize, new ScalableImage(bufferedImage));
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    int piy = 0;
    for (ImageResize imageResize : ImageResize.values()) {
      BufferedImage bufferedImage = map.get(imageResize).getScaledInstance(imageResize, param.magnify);
      graphics.drawImage(bufferedImage, 0, piy, null);
      piy += bufferedImage.getHeight();
    }
  }

  static void main() {
    launch();
  }
}
