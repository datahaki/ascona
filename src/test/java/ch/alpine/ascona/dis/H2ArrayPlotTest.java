// code by jph
package ch.alpine.ascona.dis;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.arp.H2ArrayPlot;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.ext.Serialization;

class H2ArrayPlotTest {
  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {
    Serialization.copy(new H2ArrayPlot(RealScalar.of(2)));
  }
}
