// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.cn.Complex1LieGroup;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.ComplexScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Im;
import ch.alpine.tensor.sca.Re;

public enum C1Display implements ManifoldDisplay {
  INSTANCE;

  private static final Tensor SHAPE = CirclePoints.of(9).multiply(RealScalar.of(0.12));

  @Override
  public int dimensions() {
    return 1;
  }

  @Override
  public Tensor shape() {
    return SHAPE;
  }

  @Override
  public Tensor xya2point(Tensor xya) {
    return ComplexScalar.of(xya.Get(0), xya.Get(1));
  }

  @Override
  public Tensor point2xya(Tensor p) {
    return point2xy(p).append(RealScalar.ZERO);
  }

  @Override
  public Tensor point2xy(Tensor p) {
    Scalar scalar = (Scalar) p;
    return Tensors.of( //
        Re.FUNCTION.apply(scalar), //
        Im.FUNCTION.apply(scalar));
  }

  @Override
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(point2xy(p)); //
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return Complex1LieGroup.INSTANCE;
  }

  @Override
  public TensorUnaryOperator tangentProjection(Tensor p) {
    return null;
  }

  @Override
  public LineDistance lineDistance() {
    return null;
  }

  @Override
  public RandomSampleInterface randomSampleInterface() {
    // Distribution distribution = NormalDistribution.standard();
    Distribution distribution = UniformDistribution.of(-2, 2);
    return random -> ComplexScalar.of( //
        RandomVariate.of(distribution, random), //
        RandomVariate.of(distribution, random));
  }

  @Override
  public RenderInterface background() {
    return AxesRender.INSTANCE; // EmptyRender.INSTANCE;
  }
}
