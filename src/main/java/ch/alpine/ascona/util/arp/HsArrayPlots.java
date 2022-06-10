// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.ImageReshape;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.ArrayPlotRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.math.AppendOne;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Dot;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

// TODO ASCONA
public enum HsArrayPlots {
  ;
  public static BufferedImage fuseImages(ManifoldDisplay manifoldDisplay, ArrayPlotRender arrayPlotRender, int refinement, int sequence_length) {
    BufferedImage foreground = arrayPlotRender.export();
    BufferedImage background = new BufferedImage(foreground.getWidth(), foreground.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = background.createGraphics();
    Tensor matrix = HsArrayPlots.pixel2model( //
        manifoldDisplay.coordinateBoundingBox(), //
        new Dimension(refinement, refinement));
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
            ? HsArrayPlots.cover(clip, RealScalar.ZERO)
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

  /** @param xy lower left corner
   * @param range of image in model space
   * @param dimension of image
   * @return */
  @Deprecated
  public static Tensor pixel2model(CoordinateBoundingBox coordinateBoundingBox, Dimension dimension) {
    // pixel 2 model
    Tensor xy = Tensors.of(coordinateBoundingBox.getClip(0).min(), coordinateBoundingBox.getClip(1).min());
    Tensor range = Tensors.of(coordinateBoundingBox.getClip(0).width(), coordinateBoundingBox.getClip(1).width());
    Tensor scale = Times.of(range, Tensors.vector(dimension.width, dimension.height).map(Scalar::reciprocal));
    return Dot.of( //
        GfxMatrix.translation(xy), //
        Times.of(AppendOne.FUNCTION.apply(scale), GfxMatrix.flipY(dimension.height)));
  }
}
