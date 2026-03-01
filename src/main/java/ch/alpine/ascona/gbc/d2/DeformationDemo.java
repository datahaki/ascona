// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.bas.AveragedMovingDomain2D;
import ch.alpine.ascony.bas.MovingDomain2D;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

class DeformationDemo extends AbstractDeformationDemo {
  @ReflectionMarker
  static class Param1 {
    public LogWeightings logWeightings = LogWeightings.COORDINATE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer refine = 20;
    public Scalar s2z = RealScalar.of(1);
    @FieldFuse
    public transient Boolean snap = true; // true intentional
  }

  private final Param1 param1;
  // ---
  /** in coordinate specific to geodesic display */
  private Tensor movingOrigin;

  protected DeformationDemo() {
    super(param1 = new Param1());
    fieldsEditor(1).addUniversalListener(this::recompute);
    // ---
    addChangeListener(this::shuffleSnap);
    shuffleSnap();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.DEFORM_2D;
  }

  @Override
  protected final void shuffleSnap() {
    setGeodesicControlPoints(shufflePoints(param0().length));
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

  protected Sedarim operator(Tensor sequence) {
    Manifold manifold = manifoldDisplay().manifold();
    return param1.logWeightings.sedarim(param1.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
  }

  /** @return method to compute mean (for instance approximation instead of exact mean) */
  protected MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    Tensor domain = updateDomain(movingOrigin, res, param1.s2z);
    Sedarim sedarim = operator(movingOrigin);
    return new AveragedMovingDomain2D(movingOrigin, sedarim, domain, //
        manifoldDisplay().indetPoint());
  }

  static void main() {
    new DeformationDemo().runStandalone();
  }
}
