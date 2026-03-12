// code by jph
package ch.alpine.ascona.gbc.d2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.ascony.msh.Meshgrid;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

class MeshgridTest {
  @Test
  void testSimple() {
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of(Clips.absolute(2), Clips.absolute(3));
    Tensor tensor = new Meshgrid(cbb, 20).image(t -> t);
    List<Integer> list = Dimensions.of(tensor);
    assertEquals(list, List.of(20, 20, 2));
  }
}
