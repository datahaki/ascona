// code by jph
package ch.alpine.ascona.util.ren;

import java.util.Optional;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.nrm.VectorAngle;
import ch.alpine.tensor.num.Pi;

/* package */ enum QuadShading {
  ANGLE {
    @Override
    public Scalar map(Tensor po, Tensor p0, Tensor p1, Tensor pd) {
      Optional<Scalar> optional = VectorAngle.of(p0.subtract(po), p1.subtract(po));
      return optional.map(Pi.VALUE::under).orElse(RealScalar.ZERO);
    }
  }, //
  ;

  public abstract Scalar map(Tensor po, Tensor p0, Tensor p1, Tensor pd);
}
