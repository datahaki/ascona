// code by jph
package ch.alpine.ascona.util.dat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ArrayQ;
import ch.alpine.tensor.io.ResourceData;

class GokartPoseDataTest {
  @Test
  void testSimple() {
    List<String> list = GokartPoseDataV2.INSTANCE.list();
    assertTrue(50 < list.size());
  }

  @Test
  void testResourceTensor() {
    Tensor tensor = ResourceData.of("/ch/alpine/tensor/img/colorscheme/aurora.csv"); // resource in tensor
    Objects.requireNonNull(tensor);
    assertTrue(ArrayQ.of(tensor));
  }
}
