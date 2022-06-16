// code by jph
package ch.alpine.ascona.nd;

import java.util.Optional;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.Abs;

public class S2TreeMapDemo extends AbstractTreeMapDemo {
  @Override
  ManifoldDisplay manifoldDisplay() {
    return S2Display.INSTANCE;
  }

  @Override
  Tensor center(Tensor xya) {
    Optional<Tensor> optionalZ = S2Display.optionalZ(xya);
    Tensor xyz = optionalZ.orElse(xya.extract(0, 2).append(RealScalar.ZERO));
    xyz.set(Abs.FUNCTION, 2);
    return xyz;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new S2TreeMapDemo().setVisible(1000, 800);
  }
}
