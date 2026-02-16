// code by jph
package ch.alpine.ascona.aurora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.sophis.ref.d1h.Hermite2Subdivisions;
import ch.alpine.sophis.ref.d1h.HermiteLoConfig;
import ch.alpine.tensor.Parallelize;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.exp.Log;

/* package */ class Hermite2ArrayShow extends HermiteArrayShow {
  public Hermite2ArrayShow(String name, Scalar period, int levels) {
    super(name, period, levels);
  }

  private Scalar h2(Scalar lambda, Scalar mu) {
    return process(Hermite2Subdivisions.of(HS_EXPONENTIAL, new HermiteLoConfig(lambda, mu)));
  }

  @Override // from HermiteArray
  Tensor compute(int rows, int cols) {
    Tensor mu = Subdivide.of(Rational.of(-1, 1), Rational.of(+2, 1), rows - 1);
    Tensor lambda = Subdivide.of(Rational.of(-2, 1), Rational.of(+3, 1), cols - 1);
    return Parallelize.matrix((i, j) -> h2(lambda.Get(j), mu.Get(i)), rows, cols);
  }

  static void main() throws IOException {
    int levels = 4;
    HermiteArrayShow hermiteArray = //
        new Hermite2ArrayShow("20190701T163225_01", Quantity.of(Rational.of(1, 1), "s"), levels);
    Path folder = HomeDirectory.Pictures.resolve(hermiteArray.getClass().getSimpleName(), String.format("cs_%1d", levels));
    Files.createDirectories(folder);
    Tensor matrix = hermiteArray.getMatrix();
    export(folder.resolve("id"), matrix);
    export(folder.resolve("ln"), matrix.maps(RealScalar.ONE::add).maps(Log.FUNCTION));
  }
}
