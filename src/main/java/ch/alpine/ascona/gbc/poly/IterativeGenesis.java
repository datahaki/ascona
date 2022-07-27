// code by jph
package ch.alpine.ascona.gbc.poly;

import java.util.Arrays;

import ch.alpine.sophus.dv.AffineCoordinate;
import ch.alpine.sophus.gbc.d2.Barycenter;
import ch.alpine.sophus.gbc.d2.IterativeCoordinateLevel;
import ch.alpine.sophus.gbc.d2.ThreePointWeighting;
import ch.alpine.sophus.hs.Genesis;
import ch.alpine.sophus.hs.HsDesign;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.Chop;

public enum IterativeGenesis {
  MEAN_VALUE(new ThreePointWeighting(Barycenter.MEAN_VALUE)), //
  INVERSE_DISTANCE(AffineCoordinate.INSTANCE), //
  ;

  private final Genesis genesis;

  IterativeGenesis(Genesis genesis) {
    this.genesis = genesis;
  }

  public TensorScalarFunction with(int max) {
    return new IterativeCoordinateLevel(genesis, Chop._08, max);
  }

  public static TensorUnaryOperator counts(Manifold manifold, Tensor sequence, int max) {
    HsDesign hsDesign = new HsDesign(manifold);
    TensorScalarFunction[] array = Arrays.stream(values()).map(ig -> ig.with(max)).toArray(TensorScalarFunction[]::new);
    return point -> {
      Tensor matrix = hsDesign.matrix(sequence, point);
      return Tensor.of(Arrays.stream(array).map(ig -> ig.apply(matrix)));
    };
  }
}
