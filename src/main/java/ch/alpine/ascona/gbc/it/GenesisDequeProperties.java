// code by jph
package ch.alpine.ascona.gbc.it;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.MetricBiinvariant;
import ch.alpine.sophis.gbc.amp.Amplifiers;
import ch.alpine.sophis.gbc.it.IterativeAffineCoordinate;
import ch.alpine.sophis.gbc.it.IterativeTargetCoordinate;
import ch.alpine.sophus.lie.rn.RGroup;
import ch.alpine.sophus.math.Genesis;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

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
            new MetricBiinvariant(RGroup.INSTANCE).weighting(InversePowerVariogram.of(2)), //
            beta, resolution)
        : new IterativeAffineCoordinate(tensorUnaryOperator, resolution);
  }
}
