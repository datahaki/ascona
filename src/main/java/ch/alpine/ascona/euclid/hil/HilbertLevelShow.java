// code by jph
package ch.alpine.ascona.euclid.hil;

import ch.alpine.ascony.api.ImageTiling;
import ch.alpine.ascony.api.IterativeGenesis;
import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

/* package */ enum HilbertLevelShow {
  ;
  public static Show of(Tensor sequence, int res, ColorDataGradient colorDataGradient, int max) {
    ManifoldDisplay manifoldDisplay = R2Display.INSTANCE;
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    TensorUnaryOperator tuo = IterativeGenesis.counts(homogeneousSpace, sequence, max);
    int sequence_length = IterativeGenesis.values().length;
    Tensor fallback = ConstantArray.of(DoubleScalar.INDETERMINATE, sequence_length);
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tuo, fallback);
    CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
    Tensor array = manifoldDisplay.d2Raster().of(arrayFunction, cbb, res);
    Show show = new Show();
    show.add(ArrayPlot.of(ImageTiling.of(array), colorDataGradient));
    return show;
  }
}
