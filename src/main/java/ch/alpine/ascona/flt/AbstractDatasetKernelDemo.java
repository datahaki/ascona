// code by ob, jph
package ch.alpine.ascona.flt;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.tensor.sca.win.WindowFunctions;

@ReflectionMarker
/* package */ abstract class AbstractDatasetKernelDemo extends AbstractSpectrogramDemo {
  protected final SpinnerLabel<Integer> spinnerRadius;

  protected AbstractDatasetKernelDemo(List<ManifoldDisplays> list, GokartPoseData gokartPoseData) {
    super(new GokartPoseSpec(gokartPoseData, list), new Object());
    {
      spinnerRadius = SpinnerLabel.of(IntStream.range(0, 25).boxed().toList());
      spinnerRadius.setValue(1);
      spinnerRadius.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "refinement");
      spinnerRadius.addSpinnerListener(value -> updateState());
    }
  }

  @Override // from DatasetFilterDemo
  protected String plotLabel() {
    WindowFunctions windowFunctions = gokartPoseSpec.kernel;
    int radius = spinnerRadius.getValue();
    return windowFunctions + " [" + (2 * radius + 1) + "]";
  }
}
