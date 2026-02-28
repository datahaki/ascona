// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.bas.AveragedMovingDomain2D;
import ch.alpine.ascony.bas.MovingDomain2D;
import ch.alpine.ascony.bas.RnFittedMovingDomain2D;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

// TODO ASCONA maps to target every frame right now
class R2MlsDemo extends AbstractDeformationDemo {
  @ReflectionMarker
  public static class Param0 {
    @FieldClip(min = "3", max = "12")
    public Integer length = 8;
  }

  @ReflectionMarker
  static class Param1 {
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer refine = 20;
    public Boolean r2Mls = false;
    @FieldFuse
    public transient Boolean snap = true; // true intentional
  }

  private final Param0 param0;
  private final Param1 param1;
  // ---
  /** in coordinate specific to geodesic display */
  private Tensor movingOrigin;

  protected R2MlsDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(0).addUniversalListener(this::shuffleSnap);
    fieldsEditor(1).addUniversalListener(this::recompute);
    // ---
    addChangeListener(this::shuffleSnap);
    shuffleSnap();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_ONLY;
  }

  protected final void shuffleSnap() {
    setGeodesicControlPoints(shufflePoints(param0.length));
    param1.snap = true;
    recompute();
  }

  protected final void recompute() {
    if (param1.snap) {
      param1.snap = false;
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      movingOrigin = Tensor.of(getControlPointsSe2().maps(N.DOUBLE).stream().map(manifoldDisplay::xya2point));
    }
    movingDomain2D = updateMovingDomain2D(movingOrigin, param1.refine);
  }

  protected MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    Tensor domain = updateDomain(movingOrigin, res, null);
    Biinvariants biinvariants = Biinvariants.METRIC;
    Manifold manifold = manifoldDisplay().manifold();
    if (param1.r2Mls) {
      Sedarim sedarim = LogWeightings.WEIGHTING.sedarim(biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), movingOrigin);
      return new RnFittedMovingDomain2D(movingOrigin, sedarim, domain);
    }
    Sedarim sedarim = LogWeightings.COORDINATE.sedarim(biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), movingOrigin);
    return new AveragedMovingDomain2D(movingOrigin, sedarim, domain, //
        manifoldDisplay().indetPoint());
  }

  static void main() {
    new R2MlsDemo().runStandalone();
  }
}
