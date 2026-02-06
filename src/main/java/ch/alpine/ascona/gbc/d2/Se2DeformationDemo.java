// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.sophis.crv.d2.ex.Arrowhead;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public class Se2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor ORIGIN = Arrowhead.of(RealScalar.of(0.2));

  public Se2DeformationDemo() {
    super(ManifoldDisplays.SE2C_SE2, new Object());
    // ---
    timerFrame.geometricComponent.setOffset(300, 500);
  }

  @Override
  protected synchronized Tensor shufflePointsSe2(int n) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Distribution distributionp = UniformDistribution.of(-1, 7);
    Distribution distributiona = UniformDistribution.of(-1, 1);
    return Tensors.vector(_ -> manifoldDisplay.xya2point( //
        RandomVariate.of(distributionp, 2).append(RandomVariate.of(distributiona))), n);
  }

  @Override
  protected MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    Tensor dx = Subdivide.of(0, 6, res - 1);
    Tensor dy = Subdivide.of(0, 6, res - 1);
    Tensor domain = Outer.of((cx, cy) -> Tensors.of(cx, cy, RealScalar.ZERO), dx, dy);
    return AveragedMovingDomain2D.of(movingOrigin, operator(movingOrigin), domain);
  }

  @Override
  protected Tensor shapeOrigin() {
    return ORIGIN;
  }

  @Override
  protected BiinvariantMean biinvariantMean() {
    return ((HomogeneousSpace) manifoldDisplay().geodesicSpace()).biinvariantMean();
  }

  static void main() {
    launch();
  }
}
