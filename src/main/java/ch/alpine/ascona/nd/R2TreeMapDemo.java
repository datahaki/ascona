// code by jph
package ch.alpine.ascona.nd;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.tensor.Tensor;

public class R2TreeMapDemo extends AbstractTreeMapDemo {
  @Override
  protected ManifoldDisplay manifoldDisplay() {
    return R2Display.INSTANCE;
  }

  @Override
  protected Tensor center(Tensor xya) {
    return xya.extract(0, 2);
  }

  public static void main(String[] args) {
    launch();
  }
}
