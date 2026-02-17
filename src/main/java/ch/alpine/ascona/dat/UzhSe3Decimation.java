// code by jph
package ch.alpine.ascona.dat;

import java.io.IOException;
import java.nio.file.Path;

import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.DecimationResult;
import ch.alpine.sophis.decim.Se3CurveDecimation;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Put;
import ch.alpine.tensor.qty.Timing;

/** the quaternions in the data set have norm of approximately
 * 1.00005... due to the use of float precision */
/* package */ enum UzhSe3Decimation {
  ;
  static void of(String name) throws IOException {
    System.out.println(name);
    Path root = HomeDirectory.Documents.mk_dirs("uzh", name);
    // ---
    Path file = Path.of("/media/datahaki/media/resource/uzh/groundtruth", name + ".txt");
    Tensor poses = UzhSe3TxtFormat.of(file);
    System.out.println(Dimensions.of(poses));
    Put.of(root.resolve("poses.file"), poses);
    {
      CurveDecimation curveDecimation = Se3CurveDecimation.of(RealScalar.of(0.02));
      Timing timing = Timing.started();
      DecimationResult result = curveDecimation.evaluate(poses);
      Tensor decimated = result.result();
      timing.stop();
      System.out.println(timing.seconds());
      System.out.println(Dimensions.of(decimated));
      Put.of(root.resolve("decimated.file"), decimated);
      Put.of(root.resolve("error.file"), result.errors());
    }
  }

  static void main() throws IOException {
    for (UzhSe3Data uzhSe3Data : UzhSe3Data.values()) {
      of(uzhSe3Data.name());
    }
  }
}
