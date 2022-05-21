// code by jph
package ch.alpine.ascona.util.arp;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.tensor.ext.Serialization;
import ch.alpine.tensor.sca.Clips;

class R2ArrayPlotTest {
  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {
    Serialization.copy(new R2ArrayPlot(Box2D.xy(Clips.unit())));
  }
}
