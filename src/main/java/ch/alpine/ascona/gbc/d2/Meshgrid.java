// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.N;

public enum Meshgrid {
  ;
  /** @param cbb
   * @param res
   * @return tensor with dimensions */
  public static Tensor image(CoordinateBoundingBox cbb, int res) {
    Tensor dx = Subdivide.intermediate_increasing(cbb.clip(0), res).maps(N.DOUBLE);
    Tensor dy = Subdivide.intermediate_decreasing(cbb.clip(1), res).maps(N.DOUBLE);
    return Tensor.of(dy.stream().map(Scalar.class::cast) //
        .map(py -> Tensor.of(dx.stream().map(Scalar.class::cast) //
            .map(px -> Unprotect.using(List.of(px, py))) //
        )));
  }
}
