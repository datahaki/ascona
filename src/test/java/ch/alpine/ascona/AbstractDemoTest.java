// code by jph
package ch.alpine.ascona;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.ascony.win.SanityCheckAbstractDemo;
import ch.alpine.bridge.cgr.InstanceDiscovery;

class AbstractDemoTest {
  @TestFactory
  Stream<DynamicTest> dynamicTests() {
    return InstanceDiscovery.of(getClass().getPackageName(), AbstractDemo.class).stream() //
        .map(instanceRecord -> DynamicTest.dynamicTest(instanceRecord.toString(), //
            () -> SanityCheckAbstractDemo.INSTANCE.accept(instanceRecord.supplier().get())));
  }
}
