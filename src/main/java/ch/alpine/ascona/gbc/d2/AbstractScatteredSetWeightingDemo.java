// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;
import java.util.Map;

import ch.alpine.ascona.ref.BiinvariantsParam;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.var.VariogramFunctions;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradients;

public abstract class AbstractScatteredSetWeightingDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class WeightingsParam {
    private final List<LogWeightings> list;
    @FieldSelectionCallback("list")
    public LogWeightings logWeightings = LogWeightings.DISTANCES;
    public VariogramFunctions variogramFunctions = VariogramFunctions.POWER;
    @FieldSelectionArray({ "0", "1/2", "1", "3/2", "7/4", "2", "5/2", "3" })
    public Scalar beta = RealScalar.of(1);
    public final BiinvariantsParam biinvariantsParam = BiinvariantsParam.fast();

    public WeightingsParam(List<LogWeightings> list) {
      this.list = list;
    }

    @ReflectionMarker
    public List<LogWeightings> list() {
      return list;
    }

    public Sedarim operator(Manifold manifold, Tensor sequence) {
      return logWeightings.sedarim(biinvariantsParam.ofSafe(manifold), variogram(), sequence);
    }

    protected final ScalarUnaryOperator variogram() {
      return variogramFunctions.of(beta);
    }

    protected final TensorScalarFunction function(Manifold manifold, Tensor sequence, Tensor values) {
      return logWeightings.function(biinvariantsParam.ofSafe(manifold), variogram(), sequence, values);
    }
  }

  @ReflectionMarker
  static class ScatteredSetParam {
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer refine = 20;
    public ColorDataGradients cdg = ColorDataGradients.CLASSIC;
    public Boolean show = true;
    public Boolean arrows = false;
  }

  protected final WeightingsParam weightingsParam;
  protected final ScatteredSetParam scatteredSetParam;

  protected AbstractScatteredSetWeightingDemo(List<LogWeightings> array) {
    this(new WeightingsParam(array));
  }

  protected AbstractScatteredSetWeightingDemo(WeightingsParam weightingsParam) {
    super(this.weightingsParam = weightingsParam, scatteredSetParam = new ScatteredSetParam());
    fieldsEditor(weightingsParam).addUniversalListener(this::recompute);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  protected final Biinvariant biinvariant() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = manifoldDisplay.manifold();
    Map<Biinvariants, Biinvariant> map = Biinvariants.all(manifold);
    return map.getOrDefault(weightingsParam.biinvariantsParam.biinvariants, Biinvariants.USANCE.ofSafe(manifold));
  }

  protected void recompute() {
    // ---
  }
}
