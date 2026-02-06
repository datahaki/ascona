// code by ob, jph
package ch.alpine.ascona.flt;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import ch.alpine.ascona.dat.gok.GokartPos;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.win.WindowFunctions;

/* package */ class SpectrogramDataExport {
  private void process(Path ROOT) throws IOException {
    List<String> dataSource = GokartPos.list();
    List<WindowFunctions> kernel = List.of(WindowFunctions.GAUSSIAN, WindowFunctions.HAMMING, WindowFunctions.BLACKMAN);
    // iterate over data
    for (String data : dataSource) {
      // iterate over Kernels
      // load data
      Tensor control = null; // FIXME gokartPoseData.importResource(data);
      for (WindowFunctions windowFunctions : kernel) {
        ScalarUnaryOperator smoothingKernel = windowFunctions.get();
        // iterate over radius
        // Create Geod. Center instance
        TensorUnaryOperator tensorUnaryOperator = GeodesicCenter.of(Se2Group.INSTANCE, smoothingKernel);
        for (int radius = 0; radius < 15; radius++) {
          // Create new Geod. Center
          Tensor refined = new CenterFilter(tensorUnaryOperator, radius).apply(control);
          System.out.println(data + smoothingKernel + radius);
          System.err.println(speeds(refined));
          // export velocities
          Export.of(ROOT.resolve("190319", data.replace('/', '_') + "_" + smoothingKernel + "_" + radius + ".csv"), refined);
        }
      }
    }
  }

  private Tensor speeds(Tensor refined) {
    TensorUnaryOperator INSTANCE = LieDifferences.of(Se2Group.INSTANCE);
    return INSTANCE.apply(refined).multiply(Quantity.of(50, "Hz")); // FIXME not universal
  }

  static void main() throws IOException {
    SpectrogramDataExport spectrogramDataExport = new SpectrogramDataExport();
    spectrogramDataExport.process(HomeDirectory.Desktop.resolve("MA/owl_export"));
  }
}
