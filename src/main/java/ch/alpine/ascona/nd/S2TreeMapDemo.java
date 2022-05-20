// code by jph
package ch.alpine.ascona.nd;

import java.util.Optional;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.Abs;

public class S2TreeMapDemo extends AbstractTreeMapDemo {
  @Override
  Tensor pointsAll(int length) {
    return RandomSample.of(S2Display.INSTANCE.randomSampleInterface(), length);
  }

  @Override
  Tensor center(Tensor xya) {
    Optional<Tensor> optionalZ = S2Display.optionalZ(xya);
    Tensor xyz = optionalZ.orElse(xya.extract(0, 2).append(RealScalar.ZERO));
    xyz.set(Abs.FUNCTION, 2);
    return xyz;
  }

  @Override
  ManifoldDisplay manifoldDisplay() {
    return S2Display.INSTANCE;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new S2TreeMapDemo().setVisible(1000, 800);
  }
}
