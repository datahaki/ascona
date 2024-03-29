// code by jph
package ch.alpine.ascona.analysis;

import java.util.Objects;

import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.lie.LeviCivitaTensor;
import ch.alpine.tensor.lie.bch.BakerCampbellHausdorff;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.exp.Log10;

public enum BchConvergenceShow {
  ;
  private static final Tensor SE2 = Tensors.fromString( //
      "{{{0, 0, 0}, {0, 0, -1}, {0, 1, 0}}, {{0, 0, 1}, {0, 0, 0}, {-1, 0, 0}}, {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}}}");
  private static final Tensor SO3 = LeviCivitaTensor.of(3).negate();
  private static final Tensor SL2 = Tensors.fromString( //
      "{{{0, 0, 0}, {0, 0, -2}, {0, 2, 0}}, {{0, 0, -2}, {0, 0, 0}, {2, 0, 0}}, {{0, -2, 0}, {2, 0, 0}, {0, 0, 0}}}").multiply(RationalScalar.HALF);

  public static void main(String[] args) {
    Show show = new Show();
    show.setPlotLabel("bch convergence");
    // show.getAxisY().setLabel("log");
    for (int index = 0; index < 3; ++index) {
      Tensor ad = null;
      String pl = "";
      switch (index) {
      case 0:
        ad = SE2;
        pl = "se2";
        break;
      case 1:
        ad = SO3;
        pl = "so3";
        break;
      case 2:
        ad = SL2;
        pl = "sl2";
        break;
      default:
        break;
      }
      if (Objects.nonNull(ad)) {
        System.out.println("algebra=" + pl);
        ad = ad.map(N.DOUBLE);
        BakerCampbellHausdorff bakerCampbellHausdorff = (BakerCampbellHausdorff) BakerCampbellHausdorff.of(ad, 7);
        Tensor series = bakerCampbellHausdorff.series( //
            Tensors.vector(+0.3, +0.23, +0.37), //
            Tensors.vector(+0.2, -0.36, +0.18));
        Tensor tensor = Tensor.of(series.stream().map(Vector2Norm::of));
        show.add(ListLinePlot.of(Range.of(0, tensor.length()), tensor.map(Log10.FUNCTION)));
        // visualRow.setLabel(pl);
      }
    }
    ShowDialog.of(show);
  }
}
