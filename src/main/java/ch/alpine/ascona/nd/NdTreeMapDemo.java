// code by jph
package ch.alpine.ascona.nd;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.tensor.Tensor;

public class NdTreeMapDemo extends AbstractTreeMapDemo {
  @Override
  ManifoldDisplay manifoldDisplay() {
    return R2Display.INSTANCE;
  }

  @Override
  Tensor center(Tensor xya) {
    return xya.extract(0, 2);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new NdTreeMapDemo().setVisible(1000, 800);
  }
}
