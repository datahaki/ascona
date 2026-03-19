// code by jph
package ch.alpine.ascona.def;

import java.util.List;

import ch.alpine.ascona.ref.BiinvariantsParam;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.msh.MovingDomain2D;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.var.VariogramFunctions;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;

class DeformationDemo extends AbstractDeformationDemo {
  @ReflectionMarker
  static class Param1 {
    public LogWeightings logWeightings = LogWeightings.COORDINATE;
    public final BiinvariantsParam biinvariantsParam = BiinvariantsParam.okay();
    public VariogramFunctions vf = VariogramFunctions.INVERSE_POWER;
    public Scalar beta = RealScalar.TWO;
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer refine = 20;
    public Scalar s2z = RealScalar.of(1);
    @FieldFuse
    public transient Boolean snap = true; // true intentional

    public ScalarUnaryOperator variogram() {
      return vf.of(beta);
    }
  }

  private final Param1 param1;
  // ---
  /** in coordinate specific to geodesic display */
  private Tensor movingOrigin;

  protected DeformationDemo() {
    super(param1 = new Param1());
    fieldsEditor(param1).addUniversalListener(this::recompute);
    // ---
    addChangeListener(this::shuffleSnap);
    shuffleSnap();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.DEFORM_2D;
  }

  @Override
  protected Tensor movingOrigin() {
    return movingOrigin;
  }

  @Override
  protected final void shuffleSnap() {
    setGeodesicControlPoints(shufflePoints(param0().length));
    param1.snap = true;
    recompute();
  }

  private void recompute() {
    if (param1.snap) {
      param1.snap = false;
      movingOrigin = getGeodesicControlPoints();
    }
    movingDomain2D = updateMovingDomain2D(param1.refine);
  }

  private Sedarim operator(Tensor sequence) {
    Manifold manifold = manifoldDisplay().manifold();
    return param1.logWeightings.sedarim(param1.biinvariantsParam.ofSafe(manifold), param1.variogram(), sequence);
  }

  /** @return method to compute mean (for instance approximation instead of exact mean) */
  private MovingDomain2D updateMovingDomain2D(int res) {
    BiinvariantMean biinvariantMean = manifoldDisplay().homogeneousSpace().biinvariantMean();
    Tensor weights = updateWeights(movingOrigin, res, param1.s2z, operator(movingOrigin));
    return new AveragedMovingDomain2D(weights, biinvariantMean, manifoldDisplay().indetPoint());
  }

  static void main() {
    new DeformationDemo().runStandalone();
  }
}
