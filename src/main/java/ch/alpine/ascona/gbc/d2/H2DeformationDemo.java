// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Dimension;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.hn.HnWeierstrassCoordinate;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;

// TODO ASCONA ugly when computation fails (probably for accuracy reasons)
public class H2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor TRIANGLE = CirclePoints.of(3).multiply(RealScalar.of(0.05));
  // ---
  private final SpinnerLabel<HnMeans> spinnerMeans = SpinnerLabel.of(HnMeans.class);

  public H2DeformationDemo() {
    super(ManifoldDisplays.H2_ONLY, LogWeightings.coordinates());
    // ---
    spinnerMeans.setValue(HnMeans.EXACT);
    spinnerMeans.addToComponentReduced(timerFrame.jToolBar, new Dimension(120, 28), "hn means");
    // ---
    shuffleSnap();
  }

  @Override // from AbstractDeformationDemo
  synchronized Tensor shufflePointsSe2(int n) {
    return Tensor.of(CirclePoints.of(n).multiply(RealScalar.of(3)).stream().map(row -> row.append(RealScalar.ZERO)));
  }

  @Override // from AbstractDeformationDemo
  MovingDomain2D updateMovingDomain2D(Tensor movingOrigin) {
    int res = refinement();
    double rad = 1.0;
    Tensor dx = Subdivide.of(-rad, rad, res - 1);
    Tensor dy = Subdivide.of(-rad, rad, res - 1);
    Tensor domain = Outer.of((cx, cy) -> HnWeierstrassCoordinate.toPoint(Tensors.of(cx, cy)), dx, dy);
    TensorUnaryOperator tensorUnaryOperator = operator(movingOrigin);
    return AveragedMovingDomain2D.of(movingOrigin, tensorUnaryOperator, domain);
  }

  @Override // from AbstractDeformationDemo
  BiinvariantMean biinvariantMean() {
    return spinnerMeans.getValue().get();
  }

  @Override // from AbstractDeformationDemo
  Tensor shapeOrigin() {
    return TRIANGLE;
  }

  public static void main(String[] args) {
    new H2DeformationDemo().setVisible(1200, 800);
  }
}