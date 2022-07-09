// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

public enum BackgroundOffscreen {
  ;
  public static BufferedImage fuseImages( //
      ManifoldDisplay manifoldDisplay, BufferedImage foreground, int sequence_length) {
    int _width = foreground.getWidth();
    int height = foreground.getHeight();
    BufferedImage background = new BufferedImage(_width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = background.createGraphics();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    CoordinateBoundingBox coordinateBoundingBox = d2Raster.coordinateBoundingBox();
    Tensor matrix = ImageRender.pixel2model(coordinateBoundingBox, height, height);
    GeometricLayer geometricLayer = new GeometricLayer(Inverse.of(matrix));
    Scalar width = coordinateBoundingBox.getClip(0).width();
    for (int count = 0; count < sequence_length; ++count) {
      Tensor xy = Tensors.of(width.multiply(RealScalar.of(count)), width.zero());
      geometricLayer.pushMatrix(GfxMatrix.translation(xy));
      manifoldDisplay.background().render(geometricLayer, graphics);
      geometricLayer.popMatrix();
    }
    graphics.drawImage(foreground, 0, 0, null);
    return background;
  }
}
