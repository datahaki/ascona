// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.TensorMap;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.red.Mean;

public class ImageResizeBlendDemo extends AbstractDemo {
  private final BufferedImage bufferedImage;
  private final BufferedImage grayscale;

  public ImageResizeBlendDemo() {
    bufferedImage = VehicleStatic.INSTANCE.bufferedImage_c();
    Tensor tensor = ImageFormat.from(bufferedImage);
    Tensor graysc = TensorMap.of(rgba -> Mean.of(rgba.extract(0, 3)), tensor, 2);
    grayscale = ImageFormat.of(graysc);
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    int width = grayscale.getWidth();
    int height = grayscale.getHeight();
    BufferedImage image = grayscale;
    for (int count = 0; count < 5; ++count) {
      graphics.drawImage(image, 0, count * height, null);
      image = imageResize_of(image, width, height);
    }
  }

  private static BufferedImage imageResize_of(BufferedImage bufferedImage, int width, int height) {
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    Graphics graphics = result.createGraphics();
    Image image = bufferedImage.getScaledInstance(width, height, Image.SCALE_REPLICATE);
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();
    return result;
  }

  public static void main(String[] args) {
    launch();
  }
}
