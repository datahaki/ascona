// code by jph, ob
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.bm.BiinvariantMeanCenter;
import ch.alpine.sophus.lie.se2.Se2BiinvariantMeans;
import ch.alpine.sophus.lie.so2.So2BiinvariantMeans;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;

/** demo of {@link Se2BiinvariantMeans}
 * 
 * illustration of three ways to average the angular component:
 * {@link So2BiinvariantMeans} */
public final class Se2BiinvariantMeanDemo extends AbstractSpectrogramDemo {
  private final SpinnerLabel<Se2BiinvariantMeans> spinnerFilters = SpinnerLabel.of(Se2BiinvariantMeans.class);
  private final SpinnerLabel<Integer> spinnerConvolution;

  public Se2BiinvariantMeanDemo() {
    super(ManifoldDisplays.SE2_ONLY); // TODO passing this param is no good !!!
    {
      spinnerFilters.setValue(Se2BiinvariantMeans.LINEAR);
      spinnerFilters.addToComponent(timerFrame.jToolBar, "se2 biinvariant mean");
      spinnerFilters.addSpinnerListener(_ -> updateState());
    }
    {
      spinnerConvolution = SpinnerLabel.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
      spinnerConvolution.setValue(1);
      spinnerConvolution.addToComponent(timerFrame.jToolBar, "convolution");
      spinnerConvolution.addSpinnerListener(_ -> updateState());
    }
    // ---
    updateState();
  }

  @Override
  protected void updateState() {
    super.updateState();
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ScalarUnaryOperator smoothingKernel = gokartPoseSpec.kernel.get();
    Se2BiinvariantMeans se2BiinvariantMean = spinnerFilters.getValue();
    TensorUnaryOperator tensorUnaryOperator = BiinvariantMeanCenter.of(se2BiinvariantMean, smoothingKernel);
    return Nest.of( //
        new CenterFilter(tensorUnaryOperator, param.radius), //
        control(), spinnerConvolution.getValue());
  }

  static void main() {
    new Se2BiinvariantMeanDemo().runStandalone();
  }
}
