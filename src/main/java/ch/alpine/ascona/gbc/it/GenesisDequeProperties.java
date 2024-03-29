// code by jph
package ch.alpine.ascona.gbc.it;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.MetricBiinvariant;
import ch.alpine.sophus.gbc.amp.Amplifiers;
import ch.alpine.sophus.gbc.it.IterativeAffineCoordinate;
import ch.alpine.sophus.gbc.it.IterativeTargetCoordinate;
import ch.alpine.sophus.hs.Genesis;
import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.api.TensorUnaryOperator;

@ReflectionMarker
public class GenesisDequeProperties {
  public Boolean lagrange = false;
  public Amplifiers amplifiers = Amplifiers.EXP;
  public Scalar beta = RealScalar.of(3);
  public Integer refine = 20;

  public Genesis genesis() {
    int resolution = refine;
    TensorUnaryOperator tensorUnaryOperator = amplifiers.supply(beta);
    return lagrange //
        ? new IterativeTargetCoordinate( //
            new MetricBiinvariant(RnGroup.INSTANCE).weighting(InversePowerVariogram.of(2)), //
            beta, resolution)
        : new IterativeAffineCoordinate(tensorUnaryOperator, resolution);
  }
}
