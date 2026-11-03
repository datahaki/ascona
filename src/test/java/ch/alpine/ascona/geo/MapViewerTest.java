// code by jph
package ch.alpine.ascona.geo;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.geo.Tile;
import ch.alpine.sophis.crv.d2.PolygonArea;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.Sign;

class MapViewerTest {
  @Test
  void test() {
    Tile tile = new Tile(8, 2, 4);
    Tensor polygon = tile.cbb_lon_lat();
    Scalar area = PolygonArea.of(polygon);
    Sign.requirePositive(area);
  }
}
