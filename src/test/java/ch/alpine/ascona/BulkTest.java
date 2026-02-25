// code by jph
package ch.alpine.ascona;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.cgr.InstanceDiscovery;

class BulkTest {
  @Test
  void simple() {
    List<Supplier<AbstractDemo>> list = InstanceDiscovery.of(getClass().getPackageName(), AbstractDemo.class);
    IO.println(list.size());
  }
}
