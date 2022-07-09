// code by jph
package ch.alpine.ascona.util.api;

import java.util.List;

import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.dv.InverseCoordinate;
import ch.alpine.sophus.dv.KrigingCoordinate;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.itp.CrossAveraging;
import ch.alpine.sophus.itp.Kriging;
import ch.alpine.sophus.lie.rn.RnBiinvariantMean;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public enum LogWeightings implements LogWeighting {
  DISTANCES {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.distances(sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      throw new UnsupportedOperationException();
    }
  },
  // ---
  WEIGHTING {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.weighting(variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          sedarim(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  COORDINATE {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.coordinate(variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          sedarim(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  LAGRAINATE {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return biinvariant.lagrainate(variogram, sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          sedarim(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  // ---
  /** produces affine weights
   * restricted to certain variograms, e.g. power(1.5) */
  KRIGING {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      Sedarim sedarim = biinvariant.var_dist(variogram, sequence);
      return Kriging.barycentric(sedarim, sequence)::estimate;
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      Sedarim sedarim = //
          biinvariant.var_dist(variogram, sequence);
      Kriging kriging = Kriging.interpolation(sedarim, sequence, values);
      return point -> (Scalar) kriging.estimate(point);
    }
  },
  // ---
  KRIGING_COORDINATE {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return new KrigingCoordinate( //
          biinvariant.hsDesign(), //
          biinvariant.var_dist(variogram, sequence), //
          sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          sedarim(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  },
  INVERSE_COORDINATE {
    @Override // from LogWeighting
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return new InverseCoordinate( //
          biinvariant.hsDesign(), //
          biinvariant.var_dist(variogram, sequence), //
          sequence);
    }

    @Override // from LogWeighting
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = new CrossAveraging( //
          sedarim(biinvariant, variogram, sequence), //
          RnBiinvariantMean.INSTANCE, values);
      return point -> (Scalar) tensorUnaryOperator.apply(point);
    }
  }, //
  ;

  public ScalarUnaryOperator variogramForInterpolation() {
    return name().startsWith(KRIGING.name()) || equals(INVERSE_COORDINATE) //
        ? s -> s
        : InversePowerVariogram.of(2);
  }

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

  public static List<LogWeightings> noDistances() {
    return List.of( //
        WEIGHTING, //
        COORDINATE, //
        LAGRAINATE, //
        KRIGING, //
        KRIGING_COORDINATE, //
        INVERSE_COORDINATE);
  }

  public boolean forceMetric() {
    return List.of( //
        KRIGING, //
        KRIGING_COORDINATE, //
        INVERSE_COORDINATE).contains(this);
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
