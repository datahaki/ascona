// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.var.VariogramFunctions;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;

public abstract class AbstractScatteredSetWeightingDemo extends ControlPointsDemo {
  protected final SpinnerLabel<Integer> spinnerRefine;
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  protected final JToggleButton jToggleArrows = new JToggleButton("arrows");
  protected final List<LogWeighting> array;

  public AbstractScatteredSetWeightingDemo( //
      boolean addRemoveControlPoints, //
      List<ManifoldDisplays> list, //
      List<LogWeighting> array) {
    super(new AsconaParam(addRemoveControlPoints, list));
    {
      spinnerLogWeighting = SpinnerLabel.of(array);
      if (array.contains(LogWeightings.COORDINATE))
        spinnerLogWeighting.setValue(LogWeightings.COORDINATE);
      else
        spinnerLogWeighting.setValue(array.get(0));
      if (1 < array.size())
        spinnerLogWeighting.addToComponentReduced(timerFrame.jToolBar, new Dimension(150, 28), "weights");
    }
    timerFrame.jToolBar.addSeparator();
    spinnerLogWeighting.addSpinnerListener(spinnerListener);
    {
      spinnerBiinvariant.setValue(Biinvariants.LEVERAGES);
      spinnerBiinvariant.addToComponentReduced(timerFrame.jToolBar, new Dimension(100, 28), "distance");
      spinnerBiinvariant.addSpinnerListener(v -> recompute());
    }
    spinnerVariogram.setValue(VariogramFunctions.INVERSE_POWER);
    spinnerVariogram.addToComponentReduced(timerFrame.jToolBar, new Dimension(230, 28), "variograms");
    spinnerVariogram.addSpinnerListener(v -> recompute());
    {
      spinnerBeta = SpinnerLabel.of(BETAS.stream().map(Scalar.class::cast).toList());
      spinnerBeta.setValue(RealScalar.of(2));
      spinnerBeta.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "beta");
      spinnerBeta.addSpinnerListener(v -> recompute());
    }
    timerFrame.jToolBar.addSeparator();
    this.array = array;
    controlPointsRender.setMidpointIndicated(false);
    spinnerLogWeighting.addSpinnerListener(v -> recompute());
    {
      spinnerRefine = SpinnerLabel.of(3, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 120, 160);
      spinnerRefine.setValue(20);
      spinnerRefine.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "refinement");
      spinnerRefine.addSpinnerListener(v -> recompute());
    }
    spinnerColorData.setValue(ColorDataGradients.CLASSIC);
    spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(120, 28), "color scheme");
    spinnerColorData.addSpinnerListener(v -> recompute());
    {
      jToggleArrows.setSelected(false);
      timerFrame.jToolBar.add(jToggleArrows);
    }
    // ---
    timerFrame.jToolBar.addSeparator();
  }

  protected final int refinement() {
    return spinnerRefine.getValue();
  }

  protected final ColorDataGradient colorDataGradient() {
    return spinnerColorData.getValue();
  }

  private static final Tensor BETAS = Tensors.fromString("{0, 1/2, 1, 3/2, 7/4, 2, 5/2, 3}");
  // ---
  // TODO ASCONA DEMO manage by reflection and simplify class structure
  protected final SpinnerLabel<LogWeighting> spinnerLogWeighting;
  private final SpinnerLabel<Biinvariants> spinnerBiinvariant = SpinnerLabel.of(Biinvariants.class);
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
        setBitype(Biinvariants.HARBOR);
        spinnerBeta.setValue(RationalScalar.of(3, 2));
      }
    }
  };

  protected final void setBitype(Biinvariants bitype) {
    spinnerBiinvariant.setValue(bitype);
  }

  protected final Biinvariant biinvariant() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Map<Biinvariants, Biinvariant> map = Biinvariants.all(manifold);
    Biinvariants biinvariants = bitype();
    return map.getOrDefault(biinvariants, Biinvariants.LEVERAGES.ofSafe(manifold));
  }

  protected final Biinvariants bitype() {
    return spinnerBiinvariant.getValue();
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
