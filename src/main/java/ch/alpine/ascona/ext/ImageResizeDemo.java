// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;

public class ImageResizeDemo extends AbstractDemo {
  private final BufferedImage bufferedImage;

  public ImageResizeDemo() {
    bufferedImage = VehicleStatic.INSTANCE.bufferedImage_c();
  }

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
    for (int hint : hints) {
      Image image = bufferedImage.getScaledInstance(width + 30, height + 30, hint);
      graphics.drawImage(image, 0, count * (height + 30), null);
      ++count;
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
