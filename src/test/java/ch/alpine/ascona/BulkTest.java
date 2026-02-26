// code by jph
package ch.alpine.ascona;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.cgr.InstanceDiscovery;
import ch.alpine.bridge.cgr.InstanceRecord;

class BulkTest {
  @Test
  void simple() {
    List<InstanceRecord<AbstractDemo>> list = //
        InstanceDiscovery.of(getClass().getPackageName(), AbstractDemo.class);
    list.forEach(e -> {
      IO.println(e.friendly());
      IO.println(e.toString());
    });
    IO.println(list.size());
  }
}
