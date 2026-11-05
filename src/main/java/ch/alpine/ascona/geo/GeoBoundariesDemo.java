// code by jph
package ch.alpine.ascona.geo;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.JLabel;

import ch.alpine.bridge.fig.PlotOption;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.fig.plt.StringPlot;
import ch.alpine.bridge.fig.plt.StringPlot.StringItem;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.PolygonArea;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.col.ColorDataLists;
import ch.alpine.tensor.red.Tally;

@ReflectionMarker
class GeoBoundariesDemo implements ManipulateProvider {
  public final BoundariesParam boundariesParam = new BoundariesParam();

  @Override
  public Container getContainer() {
    Tensor tensor = GeoBoundaries.INSTANCE.cache_apply(boundariesParam.country, boundariesParam.epsilon());
    if (Objects.isNull(tensor)) {
      return new JLabel("no can do");
    }
    List<Show> list = new ArrayList<>();
    for (Tensor path : tensor) {
      if (1 < path.length()) {
      }
      // Integer[] labels = Dbscan.of(path, NdCenters.VECTOR_2_NORM, RealScalar.ZERO, 1);
      long count = path.stream().distinct().count();
      Map<Tensor, Long> map = Tally.of(path);
      Scalar area = PolygonArea.of(path);
      // ---
      Show show = new Show();
      {
        Showable showable = show.add(PolygonPlot.of(path, PlotOption.FILL));
        int count2 = (int) map.values().stream().distinct().count();
        showable.setColor(ColorDataLists._097.cyclic().getColor(count2 - 1));
      }
      {
        Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
        List<StringItem> text = new LinkedList<>();
        for (Entry<Tensor, Long> entry : map.entrySet()) {
          if (1 < entry.getValue()) {
            show.add(ListPlot.of(Tensors.of(entry.getKey())));
            text.add(new StringItem(entry.getKey(), font, Color.BLACK, "" + entry.getValue()));
          }
        }
        show.add(StringPlot.of(text));
      }
      show.setAspectRatioOne();
      show.setShowLabel("area=" + area + " " + path.length());
      list.add(show);
    }
    return ShowGridComponent.of(list);
  }

  static void main() {
    new GeoBoundariesDemo().runStandalone();
  }
}
