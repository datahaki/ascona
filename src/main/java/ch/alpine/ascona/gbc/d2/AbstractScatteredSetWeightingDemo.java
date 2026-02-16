// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import ch.alpine.ascony.api.LogWeighting;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.math.api.Manifold;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.sca.var.VariogramFunctions;

public abstract class AbstractScatteredSetWeightingDemo extends ControlPointsDemo {
  protected final ScatteredSetParam scatteredSetParam;
  protected final SpinnerLabel<LogWeighting> spinnerLogWeighting;
  private final SpinnerLabel<VariogramFunctions> spinnerVariogram = SpinnerLabel.of(VariogramFunctions.class);
  private final SpinnerLabel<Scalar> spinnerBeta;
  private final SpinnerListener<LogWeighting> spinnerListener = new SpinnerListener<>() {
    @Override
    public void spun(LogWeighting logWeighting) {
      {
        boolean enabled = !logWeighting.equals(LogWeightings.DISTANCES);
        spinnerVariogram.setEnabled(enabled);
        spinnerBeta.setEnabled(enabled);
      }
      if (logWeighting.equals(LogWeightings.DISTANCES)) {
        spinnerVariogram.setValue(VariogramFunctions.POWER);
        spinnerBeta.setValue(RealScalar.of(1));
      }
      if ( //
      logWeighting.equals(LogWeightings.WEIGHTING) || //
          logWeighting.equals(LogWeightings.COORDINATE) || //
          logWeighting.equals(LogWeightings.LAGRAINATE)) {
        spinnerVariogram.setValue(VariogramFunctions.INVERSE_POWER);
        spinnerBeta.setValue(RealScalar.of(2));
      }
      if ( //
      logWeighting.equals(LogWeightings.KRIGING) || //
          logWeighting.equals(LogWeightings.KRIGING_COORDINATE)) {
        spinnerVariogram.setValue(VariogramFunctions.POWER);
        scatteredSetParam.biinvariants = Biinvariants.HARBOR;
        spinnerBeta.setValue(Rational.of(3, 2));
      }
    }
  };

  protected AbstractScatteredSetWeightingDemo( //
      List<ManifoldDisplays> list, //
      List<LogWeighting> array) {
    this(list, array, new ScatteredSetParam());
  }

  protected AbstractScatteredSetWeightingDemo( //
      List<ManifoldDisplays> list, //
      List<LogWeighting> array, ScatteredSetParam scatteredSetParam) {
    super(new AsconaParam(true, list), scatteredSetParam);
    fieldsEditor(1).addUniversalListener(this::recompute);
    this.scatteredSetParam = scatteredSetParam;
    {
      spinnerLogWeighting = SpinnerLabel.of(array);
      if (array.contains(LogWeightings.COORDINATE))
        spinnerLogWeighting.setValue(LogWeightings.COORDINATE);
      else
        spinnerLogWeighting.setValue(array.getFirst());
      if (1 < array.size())
        spinnerLogWeighting.addToComponent(timerFrame.jToolBar, "weights");
    }
    spinnerLogWeighting.addSpinnerListener(spinnerListener);
    spinnerVariogram.setValue(VariogramFunctions.INVERSE_POWER);
    spinnerVariogram.addToComponent(timerFrame.jToolBar, "variograms");
    spinnerVariogram.addSpinnerListener(_ -> recompute());
    {
      spinnerBeta = SpinnerLabel.of(BETAS.stream().map(Scalar.class::cast).toList());
      spinnerBeta.setValue(RealScalar.of(2));
      spinnerBeta.addToComponent(timerFrame.jToolBar, "beta");
      spinnerBeta.addSpinnerListener(_ -> recompute());
    }
    controlPointsRender.setMidpointIndicated(false);
    spinnerLogWeighting.addSpinnerListener(_ -> recompute());
  }

  private static final Tensor BETAS = Tensors.fromString("{0, 1/2, 1, 3/2, 7/4, 2, 5/2, 3}");
  // ---

  protected final Biinvariant biinvariant() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Map<Biinvariants, Biinvariant> map = Biinvariants.all(manifold);
    return map.getOrDefault(scatteredSetParam.biinvariants, Biinvariants.LEVERAGES.ofSafe(manifold));
  }

  protected final Sedarim operator(Tensor sequence) {
    return logWeighting().sedarim(biinvariant(), variogram(), sequence);
  }

  protected final ScalarUnaryOperator variogram() {
    return spinnerVariogram.getValue().of(spinnerBeta.getValue());
  }

  protected final TensorScalarFunction function(Tensor sequence, Tensor values) {
    return logWeighting().function(biinvariant(), variogram(), sequence, values);
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
          if (!controlPointsRender.isPositioningOngoing())
            recompute();
          break;
        default:
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        if (controlPointsRender.isPositioningOngoing())
          recompute();
      }
    };
    // ---
    timerFrame.geometricComponent.jComponent.addMouseListener(mouseAdapter);
    timerFrame.geometricComponent.jComponent.addMouseMotionListener(mouseAdapter);
  }

  /** Hint: override is possible for customization
   * 
   * @return */
  protected LogWeighting logWeighting() {
    return spinnerLogWeighting.getValue();
  }

  protected final void setLogWeighting(LogWeighting logWeighting) {
    spinnerLogWeighting.setValue(logWeighting);
    spinnerLogWeighting.reportToAll();
  }
}
