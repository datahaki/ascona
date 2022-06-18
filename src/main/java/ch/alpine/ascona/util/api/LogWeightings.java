// code by jph
package ch.alpine.ascona.util.api;

import java.util.List;

import ch.alpine.sophus.gbc.InverseCoordinate;
import ch.alpine.sophus.gbc.KrigingCoordinate;
import ch.alpine.sophus.hs.Biinvariant;
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
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.distances(sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      throw new UnsupportedOperationException();
    }
  },
  // ---
  WEIGHTING {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.weighting(variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  COORDINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.coordinate(variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  LAGRAINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.lagrainate(variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, variogram, sequence), //
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
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      TensorUnaryOperator tensorUnaryOperator = biinvariant.var_dist(variogram, sequence);
      return Kriging.barycentric(tensorUnaryOperator, sequence)::estimate;
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = //
          biinvariant.var_dist(variogram, sequence);
      Kriging kriging = Kriging.interpolation(tensorUnaryOperator, sequence, values);
      return point -> (Scalar) kriging.estimate(point);
    }
  },
  // ---
  KRIGING_COORDINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      TensorUnaryOperator tensorUnaryOperator = biinvariant.var_dist(variogram, sequence);
      return KrigingCoordinate.of(tensorUnaryOperator, biinvariant.hsDesign(), sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  INVERSE_COORDINATE {
    @Override // from LogWeighting
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      TensorUnaryOperator tensorUnaryOperator = biinvariant.var_dist(variogram, sequence);
      return InverseCoordinate.of(tensorUnaryOperator, biinvariant.hsDesign(), sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, //
        Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          operator(biinvariant, variogram, sequence), //
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
