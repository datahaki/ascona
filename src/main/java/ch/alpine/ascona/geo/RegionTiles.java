// code by jph
package ch.alpine.ascona.geo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import ch.alpine.bridge.fig.PlotOption;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.sophis.crv.d2.PolygonArea;
import ch.alpine.sophis.crv.d2.alg.PolygonClip;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.sca.Sign;

public class RegionTiles {
  public static class LevelSet {
    final List<Tile> edge = new LinkedList<>();
    final List<Tile> intr = new LinkedList<>();
    final List<Tensor> cuts = new LinkedList<>();

    static LevelSet create0() {
      LevelSet levelSet = new LevelSet();
      levelSet.edge.add(new Tile(0, 0, 0));
      return levelSet;
    }

    public Stream<Tile> stream() {
      return Stream.concat(edge.stream(), intr.stream());
    }
  }

  final List<LevelSet> list = new ArrayList<>();

  public RegionTiles(Tensor all, int z_max) {
    LevelSet prev = LevelSet.create0();
    for (int z = 0; z < z_max; ++z) {
      LevelSet next = new LevelSet();
      for (Tile tile : prev.edge) {
        if (tile.z() != z)
          throw new IllegalArgumentException();
        Tensor cbb_lon_lat = tile.cbb_lon_lat();
        Scalar area0 = PolygonArea.of(cbb_lon_lat);
        Sign.requirePositive(area0);
        TensorUnaryOperator tuo = PolygonClip.of(cbb_lon_lat);
        for (Tensor region : all) {
          // IO.println("regionArea=" + PolygonArea.of(region) + " dims=" + Dimensions.of(region));
          Tensor tensor = tuo.apply(region);
          if (!Tensors.isEmpty(tensor)) {
            Scalar area1 = Tolerance.CHOP.apply(PolygonArea.of(tensor));
            Sign.requirePositiveOrZero(area1);
            prev.cuts.add(tensor);
            boolean exact = Tolerance.CHOP.isClose(area0, area1);
            if (exact) {
              next.intr.addAll(tile.children());
            } else
              next.edge.addAll(tile.children());
            break;
          }
        }
      }
      list.add(prev);
      for (Tile tile : prev.intr)
        next.intr.addAll(tile.children());
      prev = next;
    }
    list.add(prev);
  }

  public Stream<Tile> stream() {
    return list.stream().flatMap(LevelSet::stream);
  }

  public List<Show> shows() {
    List<Show> shows = new LinkedList<>();
    for (LevelSet levelSet : list) {
      Show show = new Show();
      show.setShowLabel(levelSet.edge.size() + " " + levelSet.intr.size());
      for (Tensor tensor : levelSet.cuts)
        show.add(PolygonPlot.of(tensor, PlotOption.FILL));
      show.setAspectRatioOne();
      shows.add(show);
    }
    return shows;
  }
}
