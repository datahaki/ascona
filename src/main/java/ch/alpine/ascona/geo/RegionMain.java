package ch.alpine.ascona.geo;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;

import ch.alpine.ascona.geo.RegionTiles.LevelSet;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.plt.Plot;
import ch.alpine.bridge.pro.ShowWindow;
import ch.alpine.sophis.crv.d2.PolygonArea;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.pdf.PDF;
import ch.alpine.tensor.pdf.c.HistogramDistribution;
import ch.alpine.tensor.sca.Abs;

class RegionMain {
  static void main() {
    Tensor all = GeoBoundaries.INSTANCE.cache_apply("Austria", RealScalar.of(0.001));
    Integers.requireEquals(all.length(), 1);
    RegionTiles regionLoad = new RegionTiles(all, 13);
    List<Show> list = new LinkedList<>();
    for (LevelSet levelSet : regionLoad.list)
      if (!levelSet.cuts.isEmpty()) {
        Scalar area = levelSet.cuts.stream() //
            .map(PolygonArea::of) //
            .map(Abs.FUNCTION) //
            .reduce(Scalar::add).orElseThrow();
        IO.println(area);
        {
          Tensor samples = Tensor.of(levelSet.cuts.stream() //
              .map(PolygonArea::of) //
              .filter(Tolerance.CHOP::nonZero));
          if (1 < samples.length()) {
            HistogramDistribution distribution = (HistogramDistribution) HistogramDistribution.of(samples);
            Showable showable = Plot.of(PDF.of(distribution)::at, distribution.support());
            Show show = new Show();
            show.add(showable);
            list.add(show);
          }
        }
      }
    JDialog jDialog = ShowWindow.asDialog(list);
    jDialog.setBounds(100, 100, 500, 500);
    jDialog.setVisible(true);
  }
}
