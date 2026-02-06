// code by jph
package ch.alpine.ascona.dat;

import java.io.IOException;

import ch.alpine.ascony.dis.S2Display;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.s.S2Loxodrome;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.sca.AbsSquared;
import ch.alpine.tensor.sca.win.WindowFunctions;

/* package */ enum LoxodromeData {
  ;
  static void main() throws IOException {
    Tensor tensor = Subdivide.of(0, 4.5, 250).map(AbsSquared.FUNCTION).map(S2Loxodrome.of(RealScalar.of(0.15)));
    Export.of(HomeDirectory.path("loxodrome_exact.csv"), tensor);
    Tensor noise = RandomVariate.of(NormalDistribution.of(0, 0.05), Dimensions.of(tensor));
    tensor = tensor.add(noise);
    tensor = Tensor.of(tensor.stream().map(Vector2Norm.NORMALIZE));
    Export.of(HomeDirectory.path("loxodrome_noise.csv"), tensor);
    GeodesicSpace geodesicSpace = S2Display.INSTANCE.geodesicSpace();
    for (WindowFunctions windowFunctions : WindowFunctions.values()) {
      ScalarUnaryOperator smoothingKernel = windowFunctions.get();
      TensorUnaryOperator tensorUnaryOperator = //
          new CenterFilter(GeodesicCenter.of(geodesicSpace, smoothingKernel), 7);
      Tensor smooth = tensorUnaryOperator.apply(tensor);
      Export.of(HomeDirectory.path("loxodrome_" + smoothingKernel + ".csv"), smooth);
    }
  }
}
