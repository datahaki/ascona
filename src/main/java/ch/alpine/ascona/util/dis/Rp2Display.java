// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Optional;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.AngleVector;
import ch.alpine.tensor.mat.IdentityMatrix;
import ch.alpine.tensor.mat.pd.Orthogonalize;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.nrm.Vector2NormSquared;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Sign;
import ch.alpine.tensor.sca.pow.Sqrt;

/** symmetric positive definite 2 x 2 matrices */
public class Rp2Display extends RpnDisplay implements D2Raster {
  private static final TensorUnaryOperator PAD_RIGHT = PadRight.zeros(3, 3);
  private static final Tensor ID3 = IdentityMatrix.of(3);
  // ---
  public static final ManifoldDisplay INSTANCE = new Rp2Display();

  // ---
  private Rp2Display() {
    super(2);
  }

  /** @param xyz normalized vector
   * @return 2 x 3 matrix with rows spanning the space tangent to given xyz */
  /* package */ static Tensor tangentSpace(Tensor xyz) {
    return Orthogonalize.of(Join.of(Tensors.of(xyz), ID3)).extract(1, 3);
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    Tensor xyz = xya.copy();
    Optional<Tensor> optional = optionalZ(xyz);
    if (optional.isPresent())
      return optional.get();
    xyz.set(RealScalar.ZERO, 2);
    // intersection of front and back hemisphere
    return Vector2Norm.NORMALIZE.apply(xyz);
  }

  @Override // from ManifoldDisplay
  public Tensor unproject(Tensor p) {
    return p.copy();
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
  }

  public Tensor projectTangent(Tensor xya) {
    return projectTangent(xya, xya.Get(2));
  }

  public Tensor projectTangent(Tensor xya, Scalar angle) {
    Tensor xyz = project(xya);
    return AngleVector.of(angle).dot(tangentSpace(xyz));
  }

  public static Optional<Tensor> optionalZ(Tensor xya) {
    Tensor xy = xya.extract(0, 2);
    Scalar normsq = Vector2NormSquared.of(xy);
    if (Scalars.lessThan(normsq, RealScalar.ONE)) {
      Scalar z = Sqrt.FUNCTION.apply(RealScalar.ONE.subtract(normsq));
      return Optional.of(xy.append(z));
    }
    return Optional.empty();
  }

  private static final Clip CLIP_Z = Clips.interval(-2.5, 1);

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor xyz) {
    Tensor frame = tangentSpace(xyz);
    Tensor skew = PAD_RIGHT.apply(Transpose.of(Tensors.of( //
        frame.get(0).extract(0, 2), //
        frame.get(1).extract(0, 2))));
    skew.set(RealScalar.ONE, 2, 2);
    Scalar r = CLIP_Z.rescale(xyz.Get(2));
    skew = Times.of(Tensors.of(r, r, RealScalar.ONE), skew);
    return GfxMatrix.translation(toPoint(xyz)).dot(skew);
  }

  @Override
  public CoordinateBoundingBox coordinateBoundingBox() {
    return Box2D.xy(Clips.absolute(1));
  }

  @Override // from GeodesicArrayPlot
  public Optional<Tensor> d2lift(Tensor point) {
    Scalar z2 = RealScalar.ONE.subtract(Vector2NormSquared.of(point));
    return Optional.ofNullable(Sign.isPositive(z2) ? Append.of(point, Sqrt.FUNCTION.apply(z2)) : null);
  }

  @Override
  public RenderInterface background() {
    return S2Background.INSTANCE;
  }
}
