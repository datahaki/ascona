// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Chop;

// TODO ASCONA ALG jToggleArrows does not have effect
public class Se2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor ORIGIN = Arrowhead.of(RealScalar.of(0.2));

  public Se2DeformationDemo() {
    super(ManifoldDisplays.SE2C_SE2, LogWeightings.coordinates());
    // ---
    timerFrame.geometricComponent.setOffset(300, 500);
    shuffleSnap();
  }

  @Override
  synchronized Tensor shufflePointsSe2(int n) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Distribution distributionp = UniformDistribution.of(-1, 7);
    Distribution distributiona = UniformDistribution.of(-1, 1);
    return Tensors.vector(i -> manifoldDisplay.xya2point( //
        RandomVariate.of(distributionp, 2).append(RandomVariate.of(distributiona))), n);
  }

  @Override
  MovingDomain2D updateMovingDomain2D(Tensor movingOrigin) {
    int res = refinement();
    Tensor dx = Subdivide.of(0, 6, res - 1);
    Tensor dy = Subdivide.of(0, 6, res - 1);
    Tensor domain = Outer.of((cx, cy) -> Tensors.of(cx, cy, RealScalar.ZERO), dx, dy);
    return AveragedMovingDomain2D.of(movingOrigin, operator(movingOrigin), domain);
  }

  @Override
  Tensor shapeOrigin() {
    return ORIGIN;
  }

  @Override
  BiinvariantMean biinvariantMean() {
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay().geodesicSpace();
    return homogeneousSpace.biinvariantMean(Chop._08);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new Se2DeformationDemo().setVisible(1200, 800);
  }
}
