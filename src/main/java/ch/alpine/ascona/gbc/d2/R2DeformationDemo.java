// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.ascony.arp.D2Raster;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;

/** moving least squares */
public class R2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor ORIGIN = CirclePoints.of(3).multiply(RealScalar.of(0.1));

  @ReflectionMarker
  public static class Param2 {
    public Boolean mls = false;
  }

  private final Param2 param2;

  public R2DeformationDemo() {
    this(new Param2());
  }

  public R2DeformationDemo(Param2 param2) {
    super(ManifoldDisplays.R2_ONLY, param2);
    this.param2 = param2;
  }

  @Override
  protected synchronized Tensor shufflePointsSe2(int n) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::point2xya));
    return tensor;
  }

  @Override
  protected MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    CoordinateBoundingBox coordinateBoundingBox = d2Raster.coordinateBoundingBox();
    Tensor domain = StaticHelper.of(coordinateBoundingBox, manifoldDisplay, res);
    Sedarim sedarim = operator(movingOrigin);
    return param2.mls //
        ? RnFittedMovingDomain2D.of(movingOrigin, sedarim, domain)
        : AveragedMovingDomain2D.of(movingOrigin, sedarim, domain);
  }

  @Override
  protected Tensor shapeOrigin() {
    return ORIGIN;
  }

  @Override
  protected BiinvariantMean biinvariantMean() {
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay().geodesicSpace();
    return homogeneousSpace.biinvariantMean();
  }

  static void main() {
    launch();
  }
}
