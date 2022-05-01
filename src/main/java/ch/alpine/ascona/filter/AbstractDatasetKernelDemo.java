// code by ob, jph
package ch.alpine.ascona.filter;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.alpine.ascona.dis.ManifoldDisplay;
import ch.alpine.ascona.dis.ManifoldDisplays;
import ch.alpine.ascona.io.GokartPoseData;
import ch.alpine.javax.swing.SpinnerLabel;
import ch.alpine.tensor.sca.win.WindowFunctions;

/* package */ abstract class AbstractDatasetKernelDemo extends AbstractSpectrogramDemo {
  protected final SpinnerLabel<Integer> spinnerRadius = new SpinnerLabel<>();

  protected AbstractDatasetKernelDemo(List<ManifoldDisplay> list, GokartPoseData gokartPoseData) {
    super(list, gokartPoseData);
    {
      spinnerRadius.setList(IntStream.range(0, 25).boxed().collect(Collectors.toList()));
      spinnerRadius.setValue(1);
      spinnerRadius.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "refinement");
      spinnerRadius.addSpinnerListener(value -> updateState());
    }
  }

  protected AbstractDatasetKernelDemo(GokartPoseData gokartPoseData) {
    this(ManifoldDisplays.CL_SE2_R2, gokartPoseData);
  }

  @Override // from DatasetFilterDemo
  protected String plotLabel() {
    WindowFunctions windowFunctions = spinnerKernel.getValue();
    int radius = spinnerRadius.getValue();
    return windowFunctions + " [" + (2 * radius + 1) + "]";
  }
}
