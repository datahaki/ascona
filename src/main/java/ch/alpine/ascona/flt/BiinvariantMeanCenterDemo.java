// code by jph, ob
package ch.alpine.ascona.flt;

import java.awt.image.BufferedImage;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.bm.BiinvariantMeanCenter;
import ch.alpine.sophus.bm.BiinvariantMean;
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
class BiinvariantMeanCenterDemo extends AbstractSpectrogramDemo {
  @ReflectionMarker
  static class Param {
    public Se2BiinvariantMeans means = Se2BiinvariantMeans.LINEAR;
    @FieldSelectionArray({ "1", "2", "3", "4", "5" })
    public Integer radius = 1;
  }

  private final Param param;

  public BiinvariantMeanCenterDemo() {
    super(param = new Param());
  }

  @Override // from RenderInterface
  protected Tensor process(Tensor control) {
    ScalarUnaryOperator smoothingKernel = specParam.kernel.get();
    BiinvariantMean biinvariantMean = param.means;
    if (getSelectedMD().equals(ManifoldDisplays.R2)) {
      biinvariantMean = manifoldDisplay().homogeneousSpace().biinvariantMean();
    }
    TensorUnaryOperator tensorUnaryOperator = BiinvariantMeanCenter.of(biinvariantMean, smoothingKernel);
    return Nest.of( //
        new CenterFilter(tensorUnaryOperator, specParam.radius), //
        control, param.radius);
  }

  @Override
  protected BufferedImage bufferedImage() {
    return null;
  }

  static void main() {
    new BiinvariantMeanCenterDemo().runStandalone();
  }
}
