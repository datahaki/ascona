// code by jph
package ch.alpine.ascona.aurora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.sophis.ref.d1h.Hermite1Subdivisions;
import ch.alpine.sophis.ref.d1h.HermiteLoConfig;
import ch.alpine.tensor.Parallelize;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.exp.Log;

/* package */ class Hermite1ArrayShow extends HermiteArrayShow {
  public Hermite1ArrayShow(String name, Scalar period, int levels) {
    super(name, period, levels);
  }

  private Scalar h1(Scalar lambda, Scalar mu) {
    return process(Hermite1Subdivisions.of(HS_EXPONENTIAL, new HermiteLoConfig(lambda, mu)));
  }

  @Override // from HermiteArray
  Tensor compute(int rows, int cols) {
    Tensor lambda = Subdivide.of(RationalScalar.of(-3, 4), RationalScalar.of(-1, 6), rows - 1).maps(N.DOUBLE);
    Tensor mu = Subdivide.of(RationalScalar.of(-2, 1), RationalScalar.of(+5, 2), cols - 1).maps(N.DOUBLE);
    return Parallelize.matrix((i, j) -> h1(lambda.Get(i), mu.Get(j)), rows, cols);
    // return Parallelize.matrix((i, j) -> lambda.Get(i), rows, cols);
  }

  static void main() throws IOException {
    String name = "20190701T163225_01";
    name = "20190701T170957_03";
    // name = "20190701T174152_03";
    int levels = 4;
    HermiteArrayShow hermiteArray = //
        new Hermite1ArrayShow(name, Quantity.of(RationalScalar.of(1, 1), "s"), levels);
    Path folder = HomeDirectory.Pictures.resolve(hermiteArray.getClass().getSimpleName(), String.format("xtb3_%1d", levels));
    Files.createDirectories(folder);
    Tensor matrix = hermiteArray.getMatrix();
    HermiteArrayShow.export(folder.resolve("id"), matrix);
    HermiteArrayShow.export(folder.resolve("ln"), matrix.maps(RealScalar.ONE::add).maps(Log.FUNCTION));
  }
}
