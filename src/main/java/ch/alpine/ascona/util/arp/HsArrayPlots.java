// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.ImageTiling;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.ArrayPlotRender;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;

public enum HsArrayPlots {
  ;
  public static <T extends Tensor> Tensor raster(HsArrayPlot hsArrayPlot, int resolution, ArrayFunction<T> arrayFunction) {
    CoordinateBoundingBox coordinateBoundingBox = hsArrayPlot.coordinateBoundingBox();
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1).map(N.DOUBLE);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1).map(N.DOUBLE);
    T fallback = arrayFunction.fallback();
    return Tensor.of(dy.stream().parallel() //
        .map(py -> Tensor.of(dx.stream() //
            .map(px -> Tensors.of(px, py)) // in R2
            .map(hsArrayPlot::raster) //
            .map(optional -> optional.map(arrayFunction.function()).orElse(fallback)))));
  }

  public static BufferedImage fuseImages(ManifoldDisplay manifoldDisplay, ArrayPlotRender arrayPlotRender, int refinement, int sequence_length) {
    BufferedImage foreground = arrayPlotRender.export();
    BufferedImage background = new BufferedImage(foreground.getWidth(), foreground.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = background.createGraphics();
    HsArrayPlot hsArrayPlot = (HsArrayPlot) manifoldDisplay;
    Tensor matrix = ImageRender.pixel2model(hsArrayPlot.coordinateBoundingBox(), refinement, refinement);
    GeometricLayer geometricLayer = new GeometricLayer(Inverse.of(matrix));
    for (int count = 0; count < sequence_length; ++count) {
      manifoldDisplay.background().render(geometricLayer, graphics);
      geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.vector(2, 0)));
    }
    graphics.drawImage(foreground, 0, 0, null);
    return background;
  }

  public static ArrayPlotRender fromTensor(Tensor wgs, int magnification, boolean coverZero, ColorDataGradient colorDataGradient) {
    Rescale rescale = new Rescale(ImageTiling.of(wgs));
    Clip clip = rescale.scalarSummaryStatistics().getClip();
    return new ArrayPlotRender( //
        rescale.result(), //
        coverZero //
            ? cover(clip, RealScalar.ZERO)
            : clip, //
        colorDataGradient, magnification);
  }

  /** @param clip
   * @param scalar
   * @return */
  /* package */ static Clip cover(Clip clip, Scalar scalar) {
    return Clips.interval( //
        Min.of(clip.min(), scalar), //
        Max.of(clip.max(), scalar));
  }
}
