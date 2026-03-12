// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.msh.MovingDomain2D;
import ch.alpine.ascony.msh.RnFittedMovingDomain2D;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

class R2MlsDemo extends AbstractDeformationDemo {
  @ReflectionMarker
  static class Param1 {
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer refine = 20;
    public Boolean r2Mls = false;
    @FieldFuse
    public transient Boolean snap = true; // true intentional
  }

  private final Param1 param1;
  // ---
  /** in coordinate specific to geodesic display */
  private Tensor movingOrigin;

  public R2MlsDemo() {
    super(param1 = new Param1());
    fieldsEditor(1).addUniversalListener(this::recompute);
    // ---
    addChangeListener(this::shuffleSnap);
    shuffleSnap();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_ONLY;
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

  private final void recompute() {
    if (param1.snap) {
      param1.snap = false;
      movingOrigin = getGeodesicControlPoints();
    }
    movingDomain2D = updateMovingDomain2D(param1.refine);
  }

  private MovingDomain2D updateMovingDomain2D(int res) {
    Biinvariants biinvariants = Biinvariants.METRIC;
    Manifold manifold = manifoldDisplay().manifold();
    if (param1.r2Mls) {
      Sedarim sedarim = LogWeightings.WEIGHTING.sedarim(biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), movingOrigin);
      Tensor weights = updateWeights(movingOrigin, res, null, sedarim);
      Tensor domain = updateWeights(movingOrigin, res, null, t -> t);
      return new RnFittedMovingDomain2D(movingOrigin, weights, domain);
    }
    Sedarim sedarim = LogWeightings.COORDINATE.sedarim(biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), movingOrigin);
    Tensor weights = updateWeights(movingOrigin, res, null, sedarim);
    return new AveragedMovingDomain2D(weights, manifoldDisplay().indetPoint());
  }

  static void main() {
    new R2MlsDemo().runStandalone();
  }
}
