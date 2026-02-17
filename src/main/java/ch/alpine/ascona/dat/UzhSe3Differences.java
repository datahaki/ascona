// code by jph
package ch.alpine.ascona.dat;

import java.io.IOException;
import java.nio.file.Path;

import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.se.SeNGroup;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Put;
import ch.alpine.tensor.sca.win.WindowFunctions;

/** the quaternions in the data set have norm of approximately
 * 1.00005... due to the use of float precision */
/* package */ enum UzhSe3Differences {
  ;
  private static final LieGroup LIE_GROUP = new SeNGroup(3);

  static void main() throws IOException {
    Path path = HomeDirectory.Ephemeral.mk_dirs(UzhSe3Differences.class.getSimpleName());
    Path file = Path.of("/media/datahaki/media/resource/uzh/groundtruth", "outdoor_forward_5_davis.txt");
    Tensor poses = UzhSe3TxtFormat.of(file).extract(0, 1200);
    System.out.println(Dimensions.of(poses));
    Put.of(path.resolve("MH_04_difficult_poses.file"), poses);
    System.out.println("differences");
    TensorUnaryOperator lieDifferences = LieDifferences.of(LIE_GROUP);
    {
      Tensor delta = lieDifferences.apply(poses);
      Put.of(path.resolve("MH_04_difficult_delta.file"), delta);
    }
    System.out.println("smooth");
    {
      TensorUnaryOperator tensorUnaryOperator = //
          new CenterFilter(GeodesicCenter.of(LIE_GROUP, WindowFunctions.GAUSSIAN.get()), 4 * 3 * 2);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      System.out.println("store");
      Put.of(path.resolve("MH_04_difficult_poses_smooth.file"), smooth);
      System.out.println("differences");
      Tensor delta = lieDifferences.apply(smooth);
      System.out.println("store");
      Put.of(path.resolve("MH_04_difficult_delta_smooth.file"), delta);
    }
  }
}
