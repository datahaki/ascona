package ch.alpine.ascona.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.geo.RegionTiles.LevelSet;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.sophis.crv.d2.PolygonArea;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.red.StandardDeviation;

class RegionTilesTest {
  @Test
  void test() {
    // Boundary boundary = new Boundary("Austria", RealScalar.of(0.001));
    // all = GeoBoundaries.INSTACE.cache.apply(boundary);
    // Export.of(HomeDirectory.Documents.resolve("austria.csv"), all.get(0));
    Tensor all = Tensors.of(Import.of("ch/alpine/ascona/geo/austria.csv"));
    // IO.println(all);
    assertEquals(all.length(), 1);
    RegionTiles regionLoad = new RegionTiles(all, 13);
    Tensor areas = Tensors.empty();
    for (LevelSet levelSet : regionLoad.list)
      if (!levelSet.cuts.isEmpty()) {
        Scalar area = levelSet.cuts.stream() //
            .map(PolygonArea::of) //
            .reduce(Scalar::add).orElse(RealScalar.ZERO);
        for (Tile tile : levelSet.intr) {
          Tensor cbb_lon_lat = tile.cbb_lon_lat();
          area = area.add(PolygonArea.of(cbb_lon_lat));
        }
        // IO.println(area + " " + levelSet.intr.size());
        areas.append(area);
      }
    // areas.append(RealScalar.of(0.002));
    // IO.println(areas);
    Scalar sdvar = StandardDeviation.ofVector(areas);
    // IO.println(sdvar);
    Tolerance.CHOP.requireZero(sdvar);
  }
}
