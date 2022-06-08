// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.arp.HsArrayPlots;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.ArrayPlotRender;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradient;

public enum HilbertLevelImage {
  ;
  public static BufferedImage of(ManifoldDisplay manifoldDisplay, Tensor sequence, int res, ColorDataGradient colorDataGradient, int max) {
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    TensorUnaryOperator tuo = IterativeGenesis.counts(homogeneousSpace, sequence, max);
    int sequence_length = IterativeGenesis.values().length;
    Tensor fallback = ConstantArray.of(DoubleScalar.INDETERMINATE, sequence_length);
    HsArrayPlot geodesicArrayPlot = manifoldDisplay.arrayPlot();
    Tensor wgs = geodesicArrayPlot.raster(res, tuo, fallback);
    ArrayPlotRender arrayPlotRender = HsArrayPlots.arrayPlotFromTensor(wgs, 1, false, colorDataGradient);
    return HsArrayPlots.fuseImages(manifoldDisplay, arrayPlotRender, res, sequence_length);
  }
}
