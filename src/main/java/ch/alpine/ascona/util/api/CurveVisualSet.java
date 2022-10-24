// code by jph
package ch.alpine.ascona.util.api;

import java.awt.BasicStroke;
import java.awt.Stroke;

import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.sophus.crv.d2.Curvature2D;
import ch.alpine.sophus.hs.r2.ArcTan2D;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Accumulate;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.alg.FoldList;
import ch.alpine.tensor.nrm.Vector2Norm;

public class CurveVisualSet {
  private static final Stroke PLOT_STROKE = new BasicStroke(1.5f);
  // ---
  private final Tensor differences;
  private final Tensor differencesNorm;
  private final Tensor curvature;
  private final Tensor arcLength0;
  private final Tensor arcLength1;

  /** @param points {{x1, y1}, {x2, y2}, ..., {xn, yn}} */
  public CurveVisualSet(Tensor points) {
    differences = Differences.of(points);
    differencesNorm = Tensor.of(differences.stream().map(Vector2Norm::of));
    curvature = Curvature2D.string(points);
    arcLength0 = Accumulate.of(differencesNorm);
    arcLength1 = FoldList.of(Tensor::add, RealScalar.ZERO, differencesNorm);
  }

  public void addCurvature(Show show) {
    show.add(new ListPlot(getArcLength1(), curvature));
  }

  public void addArcTan(Show show, Tensor refined) {
    Tensor arcTan2D = Tensor.of(differences.stream().map(ArcTan2D::of));
    Tensor extract = refined.get(Tensor.ALL, 2).extract(0, arcTan2D.length());
    Showable visualRow = show.add(new ListPlot(arcLength0, arcTan2D.subtract(extract).map(So2.MOD)));
    visualRow.setLabel("arcTan[dx, dy] - phase");
    // visualRow.setStroke(PLOT_STROKE);
  }

  public Tensor getArcLength1() {
    return arcLength1;
  }
}
