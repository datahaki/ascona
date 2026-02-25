// code by jph
package ch.alpine.ascona.flt;

import org.junit.jupiter.api.Test;

import ch.alpine.ascony.res.ResourceMapper;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.mat.MatrixQ;

class ResourceMapperTest {
  @Test
  void test() {
    ResourceMapper resourceMapper = //
        ResourceMapper.of("/ch/alpine/ascona/gokart/tpq/resource_index.vector");
    for (String string : resourceMapper.list()) {
      Tensor tensor = resourceMapper.importResource(string);
      MatrixQ.require(tensor);
    }
  }
}
