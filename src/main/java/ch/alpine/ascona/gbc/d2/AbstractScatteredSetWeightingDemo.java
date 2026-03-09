// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.var.VariogramFunctions;

public abstract class AbstractScatteredSetWeightingDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class WeightingsParam {
    private final List<LogWeightings> list;
    @FieldSelectionCallback("list")
    public LogWeightings logWeightings = LogWeightings.DISTANCES;
    public VariogramFunctions variogramFunctions = VariogramFunctions.POWER;
    @FieldSelectionArray({ "0", "1/2", "1", "3/2", "7/4", "2", "5/2", "3" })
    public Scalar beta = RealScalar.of(1);
    public Biinvariants biinvariants = Biinvariants.USANCE;

    public WeightingsParam(List<LogWeightings> list) {
      this.list = list;
    }

    @ReflectionMarker
    public List<LogWeightings> list() {
      return list;
    }

    public Sedarim operator(Manifold manifold, Tensor sequence) {
      return logWeightings.sedarim(biinvariants.ofSafe(manifold), variogram(), sequence);
    }

    protected final ScalarUnaryOperator variogram() {
      return variogramFunctions.of(beta);
    }

    protected final TensorScalarFunction function(Manifold manifold, Tensor sequence, Tensor values) {
      return logWeightings.function(biinvariants.ofSafe(manifold), variogram(), sequence, values);
    }
  }

  @ReflectionMarker
  static class ScatteredSetParam {
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer refine = 20;
    public ColorDataGradients spinnerColorData = ColorDataGradients.CLASSIC;
    public Boolean arrows = false;
  }

  protected final WeightingsParam weightingsParam;
  protected final ScatteredSetParam scatteredSetParam;
  // protected final SpinnerLabel<LogWeighting> spinnerLogWeighting;
  // private final SpinnerLabel<VariogramFunctions> spinnerVariogram = SpinnerLabel.of(VariogramFunctions.class);
  // private final SpinnerLabel<Scalar> spinnerBeta;
  // private final SpinnerListener<LogWeighting> spinnerListener = new SpinnerListener<>() {
  // @Override
  // public void spun(LogWeighting logWeighting) {
  // {
  // boolean enabled = !logWeighting.equals(LogWeightings.DISTANCES);
  // spinnerVariogram.setEnabled(enabled);
  // spinnerBeta.setEnabled(enabled);
  // }
  // if (logWeighting.equals(LogWeightings.DISTANCES)) {
  // spinnerVariogram.setValue(VariogramFunctions.POWER);
  // spinnerBeta.setValue(RealScalar.of(1));
  // }
  // if ( //
  // logWeighting.equals(LogWeightings.WEIGHTING) || //
  // logWeighting.equals(LogWeightings.COORDINATE) || //
  // logWeighting.equals(LogWeightings.LAGRAINATE)) {
  // spinnerVariogram.setValue(VariogramFunctions.INVERSE_POWER);
  // spinnerBeta.setValue(RealScalar.of(2));
  // }
  // if ( //
  // logWeighting.equals(LogWeightings.KRIGING) || //
  // logWeighting.equals(LogWeightings.KRIGING_COORDINATE)) {
  // spinnerVariogram.setValue(VariogramFunctions.POWER);
  // scatteredSetParam.biinvariants = Biinvariants.HARBOR;
  // spinnerBeta.setValue(Rational.of(3, 2));
  // }
  // }
  // };

  protected AbstractScatteredSetWeightingDemo(List<LogWeightings> array) {
    this(new WeightingsParam(array));
  }

  protected AbstractScatteredSetWeightingDemo(WeightingsParam weightingsParam) {
    super(this.weightingsParam = weightingsParam, scatteredSetParam = new ScatteredSetParam());
    fieldsEditor(0).addUniversalListener(this::recompute);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
  }

  protected final Biinvariant biinvariant() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = manifoldDisplay.manifold();
    Map<Biinvariants, Biinvariant> map = Biinvariants.all(manifold);
    return map.getOrDefault(weightingsParam.biinvariants, Biinvariants.USANCE.ofSafe(manifold));
  }

  protected void recompute() {
    // ---
  }

  public final void addMouseRecomputation() {
    MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        switch (mouseEvent.getButton()) {
        case MouseEvent.BUTTON1: // insert point
          if (!isPositioningOngoing())
            recompute();
          break;
        default:
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        if (isPositioningOngoing())
          recompute();
      }
    };
    // ---
    geometricComponent().addMouseListener(mouseAdapter);
    geometricComponent().addMouseMotionListener(mouseAdapter);
  }
}
