// code by jph
package ch.alpine.ascona.aurora;

import java.io.File;
import java.io.IOException;

import ch.alpine.sophus.ref.d1h.Hermite2Subdivisions;
import ch.alpine.sophus.ref.d1h.HermiteLoParam;
import ch.alpine.tensor.Parallelize;
import ch.alpine.tensor.RationalScalar;
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
    return process(Hermite2Subdivisions.of(HS_EXPONENTIAL, new HermiteLoParam(lambda, mu)));
  }

  @Override // from HermiteArray
  Tensor compute(int rows, int cols) {
    Tensor mu = Subdivide.of(RationalScalar.of(-1, 1), RationalScalar.of(+2, 1), rows - 1);
    Tensor lambda = Subdivide.of(RationalScalar.of(-2, 1), RationalScalar.of(+3, 1), cols - 1);
    return Parallelize.matrix((i, j) -> h2(lambda.Get(j), mu.Get(i)), rows, cols);
  }

  public static void main(String[] args) throws IOException {
    int levels = 4;
    HermiteArrayShow hermiteArray = //
        new Hermite2ArrayShow("20190701T163225_01", Quantity.of(RationalScalar.of(1, 1), "s"), levels);
    File folder = HomeDirectory.Pictures(hermiteArray.getClass().getSimpleName(), String.format("cs_%1d", levels));
    folder.mkdirs();
    Tensor matrix = hermiteArray.getMatrix();
    export(new File(folder, "id"), matrix);
    export(new File(folder, "ln"), matrix.map(RealScalar.ONE::add).map(Log.FUNCTION));
  }
}
