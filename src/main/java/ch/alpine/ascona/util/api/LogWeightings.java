// code by jph
package ch.alpine.ascona.util.api;

import java.util.List;

import ch.alpine.sophus.gbc.InverseCoordinate;
import ch.alpine.sophus.gbc.KrigingCoordinate;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.itp.CrossAveraging;
import ch.alpine.sophus.itp.Kriging;
import ch.alpine.sophus.lie.rn.RnBiinvariantMean;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public enum LogWeightings implements LogWeighting {
  DISTANCES {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.distances(manifold, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      throw new UnsupportedOperationException();
    }
  },
  // ---
  WEIGHTING {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.weighting(manifold, variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, manifold, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  COORDINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.coordinate(manifold, variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, manifold, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  LAGRAINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.lagrainate(manifold, variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, manifold, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  /** produces affine weights
   * restricted to certain variograms, e.g. power(1.5) */
  KRIGING {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      TensorUnaryOperator tensorUnaryOperator = biinvariant.var_dist(manifold, variogram, sequence);
      return Kriging.barycentric(tensorUnaryOperator, sequence)::estimate;
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = //
          biinvariant.var_dist(manifold, variogram, sequence);
      Kriging kriging = Kriging.interpolation(tensorUnaryOperator, sequence, values);
      return point -> (Scalar) kriging.estimate(point);
    }
  },
  // ---
  KRIGING_COORDINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      TensorUnaryOperator tensorUnaryOperator = biinvariant.var_dist(manifold, variogram, sequence);
      return KrigingCoordinate.of(tensorUnaryOperator, manifold, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, manifold, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  INVERSE_COORDINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence) {
      TensorUnaryOperator tensorUnaryOperator = biinvariant.var_dist(manifold, variogram, sequence);
      return InverseCoordinate.of(tensorUnaryOperator, manifold, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, manifold, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  }, //
  ;

  public static List<LogWeighting> list() {
    return List.of(values());
  }

  public static List<LogWeighting> coordinates() {
    return List.of( //
        COORDINATE, //
        LAGRAINATE, //
        KRIGING_COORDINATE, //
        INVERSE_COORDINATE);
  }

  public static List<LogWeighting> averagings() {
    return List.of( //
        WEIGHTING, //
        COORDINATE, //
        LAGRAINATE, //
        new NdTreeWeighting(4), //
        new NdTreeWeighting(6), //
        KRIGING, //
        KRIGING_COORDINATE, //
        INVERSE_COORDINATE);
  }
}
