// code by jph
package ch.alpine.ubongo;

import java.util.LinkedList;
import java.util.List;

import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subsets;
import ch.alpine.tensor.io.Primitives;

public enum Candidates {
  ;
  public static List<List<Ubongo>> candidates(int use, int count) {
    List<List<Ubongo>> values = new LinkedList<>();
    List<Ubongo> ubongos = List.of(Ubongo.values());
    for (Tensor index : Subsets.of(Range.of(0, 12), use)) {
      int sum = Primitives.toIntStream(index) //
          .map(i -> ubongos.get(i).count()) //
          .sum();
      if (sum == count) {
        // System.out.println(index);
        List<Ubongo> list = Primitives.toIntStream(index) //
            .mapToObj(ubongos::get) //
            .toList();
        values.add(list);
      }
    }
    return values;
  }

  public static void main(String[] args) {
    Show show = new Show();
    for (int use = 1; use <= 12; ++use) {
      Tensor xy = Tensors.empty();
      for (int count = 2; count <= 50; ++count) {
        List<List<Ubongo>> list = candidates(use, count);
        if (0 < list.size())
          xy.append(Tensors.vectorInt(count, list.size()));
      }
      show.add(ListLinePlot.of(xy)).setLabel("" + use);
      show.add(ListPlot.of(xy));
      if (use == 12)
        System.out.println(xy);
    }
    ShowDialog.of(show);
  }
}
