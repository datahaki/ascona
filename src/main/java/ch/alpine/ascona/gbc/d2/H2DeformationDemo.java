// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.Clips;

// TODO ASCONA ugly when computation fails (probably for accuracy reasons)
public class H2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor TRIANGLE = CirclePoints.of(3).multiply(RealScalar.of(0.05));

  @ReflectionMarker
  public static class Param2 {
    public HnMeans hnMeans = HnMeans.EXACT;
  }

  private final Param2 param2;

  public H2DeformationDemo() {
    this(new Param2());
  }

  public H2DeformationDemo(Param2 param2) {
    super(ManifoldDisplays.H2_ONLY, param2);
    this.param2 = param2;
  }

  @Override // from AbstractDeformationDemo
  protected synchronized Tensor shufflePointsSe2(int n) {
    return Tensor.of(CirclePoints.of(n).multiply(RealScalar.of(3)).stream().map(row -> row.append(RealScalar.ZERO)));
  }

  @Override // from AbstractDeformationDemo
  protected MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    Tensor domain = StaticHelper.of(Box2D.xy(Clips.absolute(1.0)), manifoldDisplay(), res);
    Sedarim sedarim = operator(movingOrigin);
    return AveragedMovingDomain2D.of(movingOrigin, sedarim, domain);
  }

  @Override // from AbstractDeformationDemo
  protected BiinvariantMean biinvariantMean() {
    return param2.hnMeans.get();
  }

  @Override // from AbstractDeformationDemo
  protected Tensor shapeOrigin() {
    return TRIANGLE;
  }

  public static void main(String[] args) {
    launch();
  }
}
