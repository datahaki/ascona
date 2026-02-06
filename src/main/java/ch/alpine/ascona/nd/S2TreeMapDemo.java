// code by jph
package ch.alpine.ascona.nd;

import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.S2Display;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.Abs;

public class S2TreeMapDemo extends AbstractTreeMapDemo {
  @Override
  protected ManifoldDisplay manifoldDisplay() {
    return S2Display.INSTANCE;
  }

  @Override
  protected Tensor center(Tensor xya) {
    Optional<Tensor> optionalZ = S2Display.optionalZ(xya);
    Tensor xyz = optionalZ.orElse(xya.extract(0, 2).append(RealScalar.ZERO));
    xyz.set(Abs.FUNCTION, 2);
    return xyz;
  }

  static void main() {
    launch();
  }
}
