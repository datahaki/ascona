// code by jph
package ch.alpine.ascona.nd;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;

public class NdTreeMapDemo extends AbstractTreeMapDemo {
  @Override
  Tensor pointsAll(int length) {
    CoordinateBoundingBox coordinateBoundingBox = CoordinateBounds.of(Tensors.vector(0, 0), Tensors.vector(10, 8));
    return RandomSample.of(BoxRandomSample.of(coordinateBoundingBox), length);
  }

  @Override
  Tensor center(Tensor xya) {
    return xya.extract(0, 2);
  }

  @Override
  ManifoldDisplay manifoldDisplay() {
    return R2Display.INSTANCE;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new NdTreeMapDemo().setVisible(1000, 800);
  }
}
