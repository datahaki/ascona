// code by jph
package ch.alpine.ascona.bd2;

import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.sophus.api.Genesis;
import ch.alpine.sophus.gbc.amp.Amplifiers;
import ch.alpine.sophus.gbc.it.IterativeAffineCoordinate;
import ch.alpine.sophus.gbc.it.IterativeTargetCoordinate;
import ch.alpine.sophus.itp.InverseDistanceWeighting;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.api.TensorUnaryOperator;

public class GenesisDequeProperties {
  public Boolean lagrange = false;
  public Amplifiers amplifiers = Amplifiers.EXP;
  public Scalar beta = RealScalar.of(3);
  @FieldInteger
  public Scalar refine = RealScalar.of(20);

  public Genesis genesis() {
    int resolution = refine.number().intValue();
    TensorUnaryOperator tensorUnaryOperator = amplifiers.supply(beta);
    return lagrange //
        ? new IterativeTargetCoordinate(new InverseDistanceWeighting(InversePowerVariogram.of(2)), beta, resolution)
        : new IterativeAffineCoordinate(tensorUnaryOperator, resolution);
  }
}
