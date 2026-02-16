// code by jph
package ch.alpine.ascona.gbc.poly;

import java.util.Arrays;

import ch.alpine.sophis.dv.AffineCoordinate;
import ch.alpine.sophis.gbc.d2.IterativeCoordinateLevel;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophis.gbc.d2.ThreePointWeighting;
import ch.alpine.sophis.math.api.Genesis;
import ch.alpine.sophus.math.api.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.Chop;

public enum IterativeGenesis {
  MEAN_VALUE(new ThreePointWeighting(ThreePointScalings.MEAN_VALUE)),
  INVERSE_DISTANCE(AffineCoordinate.INSTANCE);

  private final Genesis genesis;

  IterativeGenesis(Genesis genesis) {
    this.genesis = genesis;
  }

  public TensorScalarFunction with(int max) {
    return new IterativeCoordinateLevel(genesis, Chop._08, max);
  }

  public static TensorUnaryOperator counts(Manifold manifold, Tensor sequence, int max) {
    TensorScalarFunction[] array = Arrays.stream(values()).map(ig -> ig.with(max)).toArray(TensorScalarFunction[]::new);
    return point -> {
      Tensor matrix = manifold.exponential(point).log().slash(sequence);
      return Tensor.of(Arrays.stream(array).map(ig -> ig.apply(matrix)));
    };
  }
}
