// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotRender;
import ch.alpine.ascona.util.arp.BackgroundOffscreen;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.arp.ImageTiling;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
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
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tuo, fallback);
    Tensor wgs = D2Raster.of(hsArrayPlot, res, arrayFunction);
    ArrayPlotRender arrayPlotRender = ArrayPlotRender.rescale(ImageTiling.of(wgs), colorDataGradient, 1, false);
    return BackgroundOffscreen.fuseImages(manifoldDisplay, arrayPlotRender.export(), sequence_length);
  }
}
