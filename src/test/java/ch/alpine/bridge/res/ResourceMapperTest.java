package ch.alpine.bridge.res;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;

class ResourceMapperTest {
  @Test
  void test() {
    ResourceMapper resourceMapper = //
        ResourceMapper.of("/ch/alpine/ascona/gokart/tpq/resource_index.vector");
    for (String string : resourceMapper.list()) {
      Tensor tensor = resourceMapper.importResource(string);
      IO.println(Dimensions.of(tensor));
    }
  }
}
