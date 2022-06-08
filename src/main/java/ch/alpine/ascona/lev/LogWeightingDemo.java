// code by jph
package ch.alpine.ascona.lev;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.math.var.VariogramFunctions;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public abstract class LogWeightingDemo extends LogWeightingBase {
  private static final Tensor BETAS = Tensors.fromString("{0, 1/2, 1, 3/2, 7/4, 2, 5/2, 3}");
  // ---
  // TODO ASCONA DEMO manage by reflection and simplify class structure
  private final SpinnerLabel<Bitype> spinnerBiinvariant = SpinnerLabel.of(Bitype.class);
  private final SpinnerLabel<VariogramFunctions> spinnerVariogram = SpinnerLabel.of(VariogramFunctions.class);
  private final SpinnerLabel<Scalar> spinnerBeta = new SpinnerLabel<>();
  private final SpinnerListener<LogWeighting> spinnerListener = new SpinnerListener<>() {
    @Override
    public void actionPerformed(LogWeighting logWeighting) {
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
        setBitype(Bitype.HARBOR);
        spinnerBeta.setValue(RationalScalar.of(3, 2));
      }
    }
  };

  public LogWeightingDemo(boolean addRemoveControlPoints, List<ManifoldDisplay> list, List<LogWeighting> array) {
    super(addRemoveControlPoints, list, array);
    spinnerLogWeighting.addSpinnerListener(spinnerListener);
    {
      spinnerBiinvariant.setValue(Bitype.LEVERAGES1);
      spinnerBiinvariant.addToComponentReduced(timerFrame.jToolBar, new Dimension(100, 28), "distance");
      spinnerBiinvariant.addSpinnerListener(v -> recompute());
    }
    spinnerVariogram.addToComponentReduced(timerFrame.jToolBar, new Dimension(230, 28), "variograms");
    spinnerVariogram.addSpinnerListener(v -> recompute());
    {
      spinnerBeta.setList(BETAS.stream().map(Scalar.class::cast).collect(Collectors.toList()));
      spinnerBeta.setValue(RealScalar.of(2));
      spinnerBeta.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "beta");
      spinnerBeta.addSpinnerListener(v -> recompute());
    }
    timerFrame.jToolBar.addSeparator();
  }

  protected final void setBitype(Bitype bitype) {
    spinnerBiinvariant.setValue(bitype);
  }

  protected final Biinvariant biinvariant() {
    return bitype().from(manifoldDisplay());
  }

  protected final Bitype bitype() {
    return spinnerBiinvariant.getValue();
  }

  @Override
  protected final TensorUnaryOperator operator(Manifold manifold, Tensor sequence) {
    return logWeighting().operator( //
        biinvariant(), //
        manifold, //
        variogram(), //
        sequence);
  }

  protected final ScalarUnaryOperator variogram() {
    return spinnerVariogram.getValue().of(spinnerBeta.getValue());
  }

  @Override
  protected final TensorScalarFunction function(Tensor sequence, Tensor values) {
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay().geodesicSpace();
    return logWeighting().function( //
        biinvariant(), //
        homogeneousSpace, //
        variogram(), //
        sequence, values);
  }

  @Override
  protected void recompute() {
    // ---
  }
}
