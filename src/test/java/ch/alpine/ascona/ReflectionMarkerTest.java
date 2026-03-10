// code by jph
package ch.alpine.ascona;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.cgr.ClassDiscovery;
import ch.alpine.bridge.cgr.ClassPaths;
import ch.alpine.bridge.ref.util.ClassFieldCheck;

class ReflectionMarkerTest {
  @Test
  void test() {
    ClassFieldCheck classFieldCheck = new ClassFieldCheck();
    ClassDiscovery.execute(ClassPaths.getDefault(), classFieldCheck);
    for (Class<?> cls : classFieldCheck.getInspected()) {
      IO.println(cls);
    }
    // {
    // List<Class<?>> list = classFieldCheck.getFailures();
    // assertTrue(1 < list.size()); // the exact value doesn't matter
    // }
    // {
    // List<FieldValueRecord> list = classFieldCheck.invalidFields();
    // for (FieldValueRecord fvc : list) {
    // IO.println(fvc);
    // }
    // }
  }
}
