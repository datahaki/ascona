// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public class S2DeformationDemo extends AbstractDeformationDemo {
  private static final Tensor TRIANGLE = CirclePoints.of(3).multiply(RealScalar.of(0.05));

  @ReflectionMarker
  public static class Param2 {
    public SnMeans snMeans = SnMeans.FAST;
    @FieldSlider
    @FieldClip(min = "1/2", max = "2")
    public Scalar zHeight = RealScalar.of(1.0);
  }

  private final Param2 param2;

  public S2DeformationDemo() {
    this(new Param2());
  }

  public S2DeformationDemo(Param2 param2) {
    super(ManifoldDisplays.S2_ONLY, param2);
    this.param2 = param2;
  }

  @Override
  protected synchronized Tensor shufflePointsSe2(int n) {
    Distribution distribution = UniformDistribution.of(-0.5, 0.5);
    return Tensor.of(RandomVariate.of(distribution, n, 2).stream() //
        .map(Tensor::copy) //
        .map(row -> row.append(RealScalar.ZERO)));
  }

  @Override
  protected MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    Tensor dx = Subdivide.of(-1, 1, res - 1);
    Tensor dy = Subdivide.of(-1, 1, res - 1);
    Tensor domain = Outer.of((cx, cy) -> Vector2Norm.NORMALIZE.apply(Tensors.of(cx, cy, param2.zHeight)), dx, dy);
    Sedarim sedarim = operator(movingOrigin);
    return AveragedMovingDomain2D.of(movingOrigin, sedarim, domain);
  }

  @Override
  protected BiinvariantMean biinvariantMean() {
    return param2.snMeans.get();
  }

  @Override
  protected Tensor shapeOrigin() {
    return TRIANGLE;
  }

  public static void main(String[] args) {
    launch();
  }
}
