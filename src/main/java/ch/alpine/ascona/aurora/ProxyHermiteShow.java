// code by jph
package ch.alpine.ascona.aurora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import ch.alpine.ascona.dat.gok.GokartPosVel;
import ch.alpine.ascona.dat.gok.PosVelHz;
import ch.alpine.sophis.math.Do;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.sophus.lie.so2.So2Lift;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.QuantityMagnitude;

/* package */ abstract class ProxyHermiteShow {
  static final HomogeneousSpace HS_EXPONENTIAL = Se2CoveringGroup.INSTANCE;
  // private static final BiinvariantMean BIINVARIANT_MEAN = Se2CoveringBiinvariantMean.INSTANCE;
  static final Function<Scalar, ? extends Tensor> FUNCTION = ColorDataGradients.JET;
  private static final int ROWS = 135 * 1;
  private static final int COLS = 240 * 1;
  // ---
  private final int levels;
  private final Tensor data;
  private final Tensor control;
  private final Scalar delta;
  private final Tensor matrix;

  /** @param name "20190701T163225_01"
   * @param levels 2 */
  protected ProxyHermiteShow(String name, int levels) {
    this.levels = levels;
    Scalar rate = Quantity.of(50, "Hz");
    int delta2 = 1;
    for (int level = 0; level < levels; ++level) {
      delta2 *= 2;
      rate = rate.multiply(Rational.HALF);
    }
    delta = QuantityMagnitude.SI().in("s").apply(rate.reciprocal());
    System.out.println("delta=" + delta);
    PosVelHz posVelHz = GokartPosVel.get(name, delta2 * 20 + 1);
    data = posVelHz.getPosVelSequence(); // limit , delta2 * 20 + 1
    data.set(new So2Lift(), Tensor.ALL, 0, 2);
    System.out.println(Dimensions.of(data));
    control = Thinning.of(data, delta2);
    System.out.println(Dimensions.of(control));
    matrix = compute(ROWS, COLS);
  }

  final Scalar process(HermiteSubdivision hermiteSubdivision) {
    TensorIteration tensorIteration = hermiteSubdivision.string(delta, control);
    Tensor refined = Do.of(control, tensorIteration::iterate, levels);
    // TODO ASCONA ALG not a distance
    if (refined.length() != data.length())
      System.err.println("nonono");
    Scalar total = RealScalar.ZERO;
    for (int index = 0; index < refined.length(); ++index) {
      Tensor p = refined.get(index, 0);
      Tensor q = data.get(index, 0);
      total = total.add(Vector2Norm.between(p.extract(0, 2), q.extract(0, 2)));
    }
    return total;
  }

  final Tensor getMatrix() {
    return matrix;
  }

  abstract Tensor compute(int rows, int cols);

  public static void export(Path directory, Tensor matrix) throws IOException {
    Files.createDirectories(directory);
    for (ColorDataGradients colorDataGradients : ColorDataGradients.values()) {
      Path file = directory.resolve(String.format("%s.png", colorDataGradients));
      Export.of(file, Raster.of(matrix, colorDataGradients));
    }
  }
}
