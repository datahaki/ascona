// code by jph
package ch.alpine.ascona.util.dat;

import java.io.File;
import java.io.IOException;

import ch.alpine.sophus.flt.CenterFilter;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.se3.Se3Group;
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
  public static void main(String[] args) throws IOException {
    File file = new File("/media/datahaki/media/resource/uzh/groundtruth", "outdoor_forward_5_davis.txt");
    Tensor poses = UzhSe3TxtFormat.of(file).extract(0, 1200);
    System.out.println(Dimensions.of(poses));
    Put.of(HomeDirectory.file("MH_04_difficult_poses.file"), poses);
    System.out.println("differences");
    LieDifferences lieDifferences = new LieDifferences(Se3Group.INSTANCE);
    {
      Tensor delta = lieDifferences.apply(poses);
      Put.of(HomeDirectory.file("MH_04_difficult_delta.file"), delta);
    }
    System.out.println("smooth");
    {
      TensorUnaryOperator tensorUnaryOperator = //
          new CenterFilter(GeodesicCenter.of(Se3Group.INSTANCE, WindowFunctions.GAUSSIAN.get()), 4 * 3 * 2);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      System.out.println("store");
      Put.of(HomeDirectory.file("MH_04_difficult_poses_smooth.file"), smooth);
      System.out.println("differences");
      Tensor delta = lieDifferences.apply(smooth);
      System.out.println("store");
      Put.of(HomeDirectory.file("MH_04_difficult_delta_smooth.file"), delta);
    }
  }
}
