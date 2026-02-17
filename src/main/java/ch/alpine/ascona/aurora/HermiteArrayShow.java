// code by jph
package ch.alpine.ascona.aurora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import ch.alpine.ascona.dat.gok.GokartPosVel;
import ch.alpine.ascona.dat.gok.PosVelHz;
import ch.alpine.sophis.crv.d2.Curvature2D;
import ch.alpine.sophis.math.Do;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.sophus.lie.so2.So2Lift;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.nrm.Vector1Norm;
import ch.alpine.tensor.qty.QuantityMagnitude;

/* package */ abstract class HermiteArrayShow {
  static final HomogeneousSpace HS_EXPONENTIAL = Se2CoveringGroup.INSTANCE;
  // private static final BiinvariantMean BIINVARIANT_MEAN = Se2CoveringBiinvariantMean.INSTANCE;
  static final Function<Scalar, ? extends Tensor> FUNCTION = ColorDataGradients.JET;
  private static final int ROWS = 135 * 8;
  private static final int COLS = 240 * 8;
  // ---
  private final int levels;
  private final Tensor control = Tensors.empty();
  private final Scalar delta;
  private final Tensor matrix;

  /** @param name "20190701T163225_01"
   * @param period 1/2[s]
   * @param levels 4 */
  protected HermiteArrayShow(String name, Scalar period, int levels) {
    this.levels = Integers.requirePositive(levels);
    PosVelHz posVelHz = GokartPosVel.get(name, 1000);
    Tensor data = posVelHz.getPosVelSequence();
    data.set(new So2Lift(), Tensor.ALL, 0, 2);
    Scalar rate = posVelHz.getSamplingRate();
    delta = QuantityMagnitude.SI().in("s").apply(period);
    int skip = Scalars.intValueExact(period.multiply(rate));
    for (int index = 0; index < data.length(); index += skip)
      control.append(data.get(index));
    matrix = compute(ROWS, COLS);
  }

  final Scalar process(HermiteSubdivision hermiteSubdivision) {
    TensorIteration tensorIteration = hermiteSubdivision.string(delta, control);
    Tensor refined = Do.of(tensorIteration::iterate, levels);
    Tensor vector = Curvature2D.string(Tensor.of(refined.stream().map(point -> point.get(0).extract(0, 2))));
    // Tensor vector = Differences.of(Tensor.of(refined.stream().map(point -> point.get(1, 1))));
    // return Log.FUNCTION.apply(Norm._1.ofVector(vector).add(RealScalar.ONE));
    // Tensor vector = Flatten.of(Differences.of(Tensor.of(refined.stream().map(point -> point.get(1)))));
    // return Norm._1.ofVector(Differences.of(vector));
    return Vector1Norm.of(vector);
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
