// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.mat.re.Inverse;

public enum HsArrayPlots {
  ;
  public static BufferedImage fuseImages( //
      ManifoldDisplay manifoldDisplay, BufferedImage foreground, int refinement, int sequence_length) {
    BufferedImage background = new BufferedImage(foreground.getWidth(), foreground.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = background.createGraphics();
    HsArrayPlot hsArrayPlot = (HsArrayPlot) manifoldDisplay;
    Tensor matrix = ImageRender.pixel2model(hsArrayPlot.coordinateBoundingBox(), refinement, refinement);
    GeometricLayer geometricLayer = new GeometricLayer(Inverse.of(matrix));
    Scalar width = hsArrayPlot.coordinateBoundingBox().getClip(0).width();
    for (int count = 0; count < sequence_length; ++count) {
      manifoldDisplay.background().render(geometricLayer, graphics);
      geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.of(width, width.zero())));
    }
    graphics.drawImage(foreground, 0, 0, null);
    return background;
  }
}
