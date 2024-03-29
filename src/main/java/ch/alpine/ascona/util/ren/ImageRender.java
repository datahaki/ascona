// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.bridge.gfx.AffineTransforms;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Clip;

/** coordinate bounding box is area of image in model space */
public class ImageRender implements RenderInterface {
  /** @param coordinateBoundingBox
   * @param w
   * @param h
   * @return */
  public static Tensor pixel2model(CoordinateBoundingBox coordinateBoundingBox, int w, int h) {
    Clip clipX = coordinateBoundingBox.clip(0);
    Clip clipY = coordinateBoundingBox.clip(1);
    Tensor range = Tensors.of(clipX.width(), clipY.width());
    Tensor scale = Times.of(Tensors.vector(w, h), range.map(Scalar::reciprocal));
    Tensor mat = GfxMatrix.translation(Tensors.of(clipX.min(), clipY.min()));
    return mat.dot(Times.of(Append.of(scale.map(Scalar::reciprocal), RealScalar.ONE), GfxMatrix.flipY(h)));
  }

  public static boolean DRAW_BOX = false;
  private final BufferedImage bufferedImage;
  private final CoordinateBoundingBox coordinateBoundingBox;
  private final Tensor pixel2model;

  public ImageRender(BufferedImage bufferedImage, CoordinateBoundingBox coordinateBoundingBox) {
    this.bufferedImage = bufferedImage;
    this.coordinateBoundingBox = coordinateBoundingBox;
    pixel2model = pixel2model(coordinateBoundingBox, bufferedImage.getWidth(), bufferedImage.getHeight());
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (DRAW_BOX) {
      graphics.setColor(Color.LIGHT_GRAY);
      graphics.draw(geometricLayer.toPath2D(Box2D.polygon(coordinateBoundingBox), true));
    }
    geometricLayer.pushMatrix(pixel2model);
    graphics.drawImage(bufferedImage, AffineTransforms.of(geometricLayer.getMatrix()), null);
    geometricLayer.popMatrix();
  }
}
