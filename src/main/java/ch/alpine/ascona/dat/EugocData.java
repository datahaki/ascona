// code by jph
package ch.alpine.ascona.dat;

import java.io.IOException;

import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.io.Put;
import ch.alpine.tensor.sca.win.WindowFunctions;

/* package */ enum EugocData {
  ;
  static void main() throws IOException {
    Tensor tensor = Import.of("/dubilab/app/pose/2r/20180820T165637_2.csv");
    // System.out.println(Dimensions.of(tensor));
    Tensor poses = Tensor.of(tensor.stream() //
        .map(row -> row.extract(1, 4)));
    // Tensors.empty();
    // for (Tensor row : tensor) {
    // Tensor xyt = row.extract(1, 4);
    // poses.append(xyt);
    // }
    System.out.println(Dimensions.of(poses));
    Put.of(HomeDirectory.path("gokart_poses.file"), poses);
    TensorUnaryOperator INSTANCE = LieDifferences.of(Se2Group.INSTANCE);
    {
      Tensor delta = INSTANCE.apply(poses);
      Put.of(HomeDirectory.path("gokart_delta.file"), delta);
    }
    {
      TensorUnaryOperator tensorUnaryOperator = //
          new CenterFilter(GeodesicCenter.of(Se2Group.INSTANCE, WindowFunctions.GAUSSIAN.get()), 6);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      Put.of(HomeDirectory.path("gokart_poses_gauss.file"), smooth);
      Tensor delta = INSTANCE.apply(smooth);
      Put.of(HomeDirectory.path("gokart_delta_gauss.file"), delta);
    }
    {
      TensorUnaryOperator tensorUnaryOperator = //
          new CenterFilter(GeodesicCenter.of(Se2Group.INSTANCE, WindowFunctions.HAMMING.get()), 6);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      Put.of(HomeDirectory.path("gokart_poses_hammi.file"), smooth);
      Tensor delta = INSTANCE.apply(smooth);
      Put.of(HomeDirectory.path("gokart_delta_hammi.file"), delta);
    }
  }
}
