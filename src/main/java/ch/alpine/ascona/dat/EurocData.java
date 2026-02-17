// code by jph
package ch.alpine.ascona.dat;

import java.io.IOException;
import java.nio.file.Path;

import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.se.SeNGroup;
import ch.alpine.sophus.lie.se3.Se3Matrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.io.Put;
import ch.alpine.tensor.lie.rot.Quaternion;
import ch.alpine.tensor.lie.rot.QuaternionToRotationMatrix;
import ch.alpine.tensor.sca.win.WindowFunctions;

/** the quaternions in the data set have norm of approximately
 * 1.00005... due to the use of float precision */
/* package */ enum EurocData {
  ;
  private static Tensor rowmap(Tensor row) {
    Tensor p = row.extract(1, 4);
    Quaternion quaternion = Quaternion.of(row.Get(4), row.extract(5, 8));
    quaternion = quaternion.divide(quaternion.abs()); // normalize to unit from float to double precision
    Tensor R = QuaternionToRotationMatrix.of(quaternion);
    return Se3Matrix.of(R, p);
  }

  static void main() throws IOException {
    Path resourcePath = Unprotect.resourcePath("/ch/alpine/ascona/euroc/tpq/200Hz/MH_04_difficult.csv");
    Tensor tensor = Import.of(resourcePath);
    Path path = HomeDirectory.Ephemeral.mk_dirs(EurocData.class.getSimpleName());
    IO.println("data = " + Dimensions.of(tensor));
    // ---
    Tensor poses = Tensor.of(tensor.stream().limit(12500).map(EurocData::rowmap));
    System.out.println("maps = " + Dimensions.of(poses));
    System.out.println("differences");
    LieGroup lieGroup = new SeNGroup(3);
    TensorUnaryOperator INSTANCE = LieDifferences.of(lieGroup);
    {
      Tensor delta = INSTANCE.apply(poses);
      Put.of(path.resolve("MH_04_difficult_delta.file"), delta);
    }
    System.out.println("smooth");
    {
      TensorUnaryOperator tensorUnaryOperator = //
          new CenterFilter(GeodesicCenter.of(lieGroup, WindowFunctions.GAUSSIAN.get()), 4 * 3 * 2);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      System.out.println("store");
      Put.of(path.resolve("MH_04_difficult_poses_smooth.file"), smooth);
      System.out.println("differences");
      Tensor delta = INSTANCE.apply(smooth);
      System.out.println("store");
      Put.of(path.resolve("MH_04_difficult_delta_smooth.file"), delta);
    }
  }
}
