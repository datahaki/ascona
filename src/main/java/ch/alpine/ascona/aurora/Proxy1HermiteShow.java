// code by jph
package ch.alpine.ascona.aurora;

import java.io.IOException;
import java.nio.file.Path;

import ch.alpine.sophis.ref.d1h.Hermite1Subdivisions;
import ch.alpine.sophis.ref.d1h.HermiteLoConfig;
import ch.alpine.tensor.Parallelize;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.exp.Log;

/* package */ class Proxy1HermiteShow extends ProxyHermiteShow {
  public Proxy1HermiteShow(String name, int levels) {
    super(name, levels);
  }

  private Scalar h1(Scalar lambda, Scalar mu) {
    return process(Hermite1Subdivisions.of(HS_EXPONENTIAL, new HermiteLoConfig(lambda, mu)));
  }

  @Override // from HermiteArray
  Tensor compute(int rows, int cols) {
    Tensor lambda = Subdivide.of(Rational.of(-3, 4), Rational.of(-1, 6), rows - 1).maps(N.DOUBLE);
    Tensor mu = Subdivide.of(Rational.of(-2, 1), Rational.of(+5, 2), cols - 1).maps(N.DOUBLE);
    return Parallelize.matrix((i, j) -> h1(lambda.Get(i), mu.Get(j)), rows, cols);
    // return Parallelize.matrix((i, j) -> lambda.Get(i), rows, cols);
  }

  static void main() throws IOException {
    String name = "20190701T163225_01";
    // name = "20190701T170957_03";
    // name = "20190701T174152_03";
    int levels = 3;
    ProxyHermiteShow proxyHermite = new Proxy1HermiteShow(name, levels);
    Path folder = HomeDirectory.Pictures.mk_dirs( //
        proxyHermite.getClass().getSimpleName(), String.format("p_%1d", levels));
    Tensor matrix = proxyHermite.getMatrix();
    HermiteArrayShow.export(folder.resolve("id"), matrix);
    HermiteArrayShow.export(folder.resolve("ln"), matrix.maps(RealScalar.ONE::add).maps(Log.FUNCTION));
  }
}
