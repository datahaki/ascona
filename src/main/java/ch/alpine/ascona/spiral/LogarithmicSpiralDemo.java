// code by jph
package ch.alpine.ascona.spiral;

import ch.alpine.sophus.crv.se2c.LogarithmicSpiral;

public class LogarithmicSpiralDemo extends AbstractSpiralDemo {
  public LogarithmicSpiralDemo() {
    super(LogarithmicSpiral.of(1, 0.2));
  }

  public static void main(String[] args) {
    new LogarithmicSpiralDemo().setVisible(1000, 600);
  }
}
