// code by jph
package ch.alpine.ascona.gbc.d2;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.api.MixedLogWeightings;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.BoundingBoxRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Chop;

/** moving least squares */
public class R2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor ORIGIN = CirclePoints.of(3).multiply(RealScalar.of(0.1));
  // ---
  private final JToggleButton jToggleRigidMotionFit = new JToggleButton("MLS");

  public R2DeformationDemo() {
    super(ManifoldDisplays.R2_ONLY, MixedLogWeightings.scattered());
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    {
      jToggleRigidMotionFit.addActionListener(l -> recompute());
      timerFrame.jToolBar.add(jToggleRigidMotionFit);
    }
    timerFrame.geometricComponent.setOffset(300, 500);
    timerFrame.geometricComponent.addRenderInterfaceBackground( //
        new BoundingBoxRender(hsArrayPlot.coordinateBoundingBox()));
    setControlPointsSe2(shufflePointsSe2(7));
    // deformed to:
    // "{{1.400, 4.067, 0.000}, {2.867, 4.167, 0.000}, {1.667, 2.283, 0.000}, {3.983, 2.283, 0.000}, {2.617, 1.200, 0.000}, {0.600, 0.350, 0.000}, {3.917,
    // 1.183, 0.000}}"
    snap();
  }

  @Override
  synchronized Tensor shufflePointsSe2(int n) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::unproject));
    return tensor;
  }

  @Override
  MovingDomain2D updateMovingDomain2D(Tensor movingOrigin) {
    int res = refinement();
    // TODO ASCONA ALG meshgrid functionality is already(?)/should be generalized
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    CoordinateBoundingBox coordinateBoundingBox = hsArrayPlot.coordinateBoundingBox();
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), res - 1);
    Tensor dy = Subdivide.increasing(coordinateBoundingBox.getClip(1), res - 3);
    Tensor domain = Outer.of(Tensors::of, dx, dy);
    TensorUnaryOperator tensorUnaryOperator = operator(movingOrigin);
    return jToggleRigidMotionFit.isSelected() //
        ? RnFittedMovingDomain2D.of(movingOrigin, tensorUnaryOperator, domain)
        : AveragedMovingDomain2D.of(movingOrigin, tensorUnaryOperator, domain);
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
    LookAndFeels.LIGHT.tryUpdateUI();
    new R2DeformationDemo().setVisible(1300, 800);
  }
}
