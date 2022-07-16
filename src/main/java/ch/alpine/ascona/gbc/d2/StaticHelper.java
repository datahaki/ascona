// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.N;

enum StaticHelper {
  ;
  static <T extends Tensor> Tensor of(CoordinateBoundingBox coordinateBoundingBox, ManifoldDisplay manifoldDisplay, int resolution) {
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1).map(N.DOUBLE);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1).map(N.DOUBLE);
    
    return Tensor.of(dy.stream().map(Scalar.class::cast).parallel() //
        .map(py -> Tensor.of(dx.stream().map(Scalar.class::cast) //
            .map(px -> Unprotect.using(List.of(px, py, RealScalar.ZERO))) //
            .map(manifoldDisplay::xya2point))));
  }

}
