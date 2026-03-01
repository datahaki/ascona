// code by jph
package ch.alpine.ascona;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import ch.alpine.ascony.win.SanityCheckAscony;
import ch.alpine.bridge.cgr.InstanceDiscovery;
import ch.alpine.bridge.pro.RunProvider;

class AbstractDemoTest {
  @TestFactory
  Stream<DynamicTest> dynamicTests() {
    return InstanceDiscovery.of(getClass().getPackageName(), RunProvider.class).stream() //
        .map(instanceRecord -> DynamicTest.dynamicTest(instanceRecord.toString(), //
            () -> new SanityCheckAscony().accept(instanceRecord)));
  }
}
