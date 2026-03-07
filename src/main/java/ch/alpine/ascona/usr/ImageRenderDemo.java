// code by jph
package ch.alpine.ascona.usr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.TensorMap;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Mean;
import ch.alpine.tensor.sca.Clips;

class ImageRenderDemo extends AbstractDemo implements RenderInterface {
  private static final CoordinateBoundingBox COORDINATE_BOUNDING_BOX = //
      CoordinateBoundingBox.of(Clips.interval(-0.4, 1), Clips.interval(-0.35, 0.35));
  private static final Scalar SHIFT = RealScalar.of(1.5);
  private final BufferedImage bufferedImage_c = ResourceData.bufferedImage("/ch/alpine/curios/man/vehicle_c.png");
  private final BufferedImage bufferedImage;
  private final BufferedImage grayscale_alpha;
  private final BufferedImage grayscale;

  public ImageRenderDemo() {
    bufferedImage = bufferedImage_c;
    {
      Tensor tensor = ImageFormat.from(bufferedImage);
      Tensor graysc = TensorMap.of(rgba -> Tensors.of(Mean.of(rgba.extract(0, 3)), rgba.Get(3)), tensor, 2);
      grayscale_alpha = ImageFormat.of(graysc);
    }
    {
      Tensor tensor = ImageFormat.from(bufferedImage);
      Tensor graysc = TensorMap.of(rgba -> Mean.of(rgba.extract(0, 3)), tensor, 2);
      grayscale = ImageFormat.of(graysc);
    }
    geometricComponent().addRenderInterface(this);
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor mouse = geometricComponent().getMouseSe2CState();
    {
      geometricLayer.pushMatrix(Se2Matrix.of(mouse));
      new ImageRender( //
          bufferedImage, //
          COORDINATE_BOUNDING_BOX).render(geometricLayer, graphics);
      geometricLayer.popMatrix();
    }
    mouse.set(SHIFT::add, 0);
    {
      geometricLayer.pushMatrix(Se2Matrix.of(mouse));
      new ImageRender( //
          grayscale_alpha, //
          COORDINATE_BOUNDING_BOX).render(geometricLayer, graphics);
      geometricLayer.popMatrix();
    }
    mouse.set(SHIFT::add, 0);
    {
      geometricLayer.pushMatrix(Se2Matrix.of(mouse));
      new ImageRender( //
          grayscale, //
          COORDINATE_BOUNDING_BOX).render(geometricLayer, graphics);
      geometricLayer.popMatrix();
    }
  }

  static void main() {
    new ImageRenderDemo().runStandalone();
  }
}
