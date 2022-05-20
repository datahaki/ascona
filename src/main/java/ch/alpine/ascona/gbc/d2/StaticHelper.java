// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.ImageReshape;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.ArrayPlotRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

/* package */ enum StaticHelper {
  ;
  public static BufferedImage fuseImages(ManifoldDisplay manifoldDisplay, ArrayPlotRender arrayPlotRender, int refinement, int sequence_length) {
    HsArrayPlot geodesicArrayPlot = manifoldDisplay.arrayPlot();
    BufferedImage foreground = arrayPlotRender.export();
    BufferedImage background = new BufferedImage(foreground.getWidth(), foreground.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = background.createGraphics();
    Tensor matrix = geodesicArrayPlot.pixel2model(new Dimension(refinement, refinement));
    GeometricLayer geometricLayer = new GeometricLayer(Inverse.of(matrix));
    for (int count = 0; count < sequence_length; ++count) {
      manifoldDisplay.background().render(geometricLayer, graphics);
      geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.vector(2, 0)));
    }
    graphics.drawImage(foreground, 0, 0, null);
    return background;
  }

  public static ArrayPlotRender arrayPlotFromTensor(Tensor wgs, int magnification, boolean coverZero, ColorDataGradient colorDataGradient) {
    Rescale rescale = new Rescale(ImageReshape.of(wgs));
    Clip clip = rescale.scalarSummaryStatistics().getClip();
    return new ArrayPlotRender( //
        rescale.result(), //
        coverZero //
            ? StaticHelper.cover(clip, RealScalar.ZERO)
            : clip, //
        colorDataGradient, magnification);
  }

  /** @param clip
   * @param scalar
   * @return */
  public static Clip cover(Clip clip, Scalar scalar) {
    return Clips.interval( //
        Min.of(clip.min(), scalar), //
        Max.of(clip.max(), scalar));
  }
}
