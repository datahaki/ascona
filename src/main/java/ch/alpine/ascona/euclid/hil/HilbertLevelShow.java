// code by jph
package ch.alpine.ascona.euclid.hil;

import ch.alpine.ascony.api.IterativeGenesis;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.plt.DensityPlot;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;

/* package */ enum HilbertLevelShow {
  ;
  public static Showable of( //
      IterativeGenesis iterativeGenesis, //
      Tensor sequence, //
      int res, //
      ColorDataGradient colorDataGradient, //
      int max) {
    ManifoldDisplay manifoldDisplay = R2Display.INSTANCE;
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    TensorScalarFunction tsf = iterativeGenesis.counts(homogeneousSpace, sequence, max);
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(t -> {
      try {
        return tsf.apply(t);
      } catch (Exception e) {
        System.err.println("fail: " + t);
        return DoubleScalar.INDETERMINATE;
      }
    }, DoubleScalar.INDETERMINATE);
    CoordinateBoundingBox cbb = CoordinateBounds.of(sequence);
    Tensor array = manifoldDisplay.d2Raster().of(arrayFunction, cbb, res);
    return DensityPlot.of(array, cbb, colorDataGradient);
  }
}
