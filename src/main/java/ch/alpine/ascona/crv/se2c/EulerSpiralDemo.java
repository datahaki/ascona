// code by jph
package ch.alpine.ascona.crv.se2c;

import ch.alpine.sophus.crv.se2c.EulerSpiral;

public class EulerSpiralDemo extends AbstractSpiralDemo {
  public EulerSpiralDemo() {
    super(EulerSpiral.FUNCTION);
  }

  public static void main(String[] args) {
    new EulerSpiralDemo().setVisible(1000, 600);
  }
}