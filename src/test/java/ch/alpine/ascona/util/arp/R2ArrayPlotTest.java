// code by jph
package ch.alpine.ascona.util.arp;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.ext.Serialization;

class R2ArrayPlotTest {
  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {
    Serialization.copy(R2ArrayPlot.of(RealScalar.of(3)));
  }
}
