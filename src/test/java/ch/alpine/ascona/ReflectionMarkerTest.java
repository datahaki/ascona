// code by jph
package ch.alpine.ascona;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.cgr.ClassDiscovery;
import ch.alpine.bridge.cgr.ClassPaths;
import ch.alpine.bridge.ref.util.ClassFieldCheck;
import ch.alpine.bridge.ref.util.FieldValueRecord;

class ReflectionMarkerTest {
  @Test
  void test() {
    ClassFieldCheck classFieldCheck = new ClassFieldCheck();
    ClassDiscovery.execute(ClassPaths.getDefault(), classFieldCheck);
    assertTrue(100 < classFieldCheck.getInspected().size());
    for (Class<?> cls : classFieldCheck.getFailures())
      IO.println(cls);
    assertTrue(classFieldCheck.getFailures().isEmpty());
    // ---
    for (FieldValueRecord fvc : classFieldCheck.invalidFields())
      IO.println(fvc);
    assertTrue(classFieldCheck.invalidFields().isEmpty());
  }
}
