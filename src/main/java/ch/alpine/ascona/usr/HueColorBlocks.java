// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.PackageTestAccess;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorFormat;
import ch.alpine.tensor.img.Hue;
import ch.alpine.tensor.img.StrictColorDataIndexed;
import ch.alpine.tensor.sca.Mod;

/* package */ enum HueColorBlocks {
  ;
  private static final Mod MOD = Mod.function(1);
  @PackageTestAccess
  /* package */ static final Scalar GOLDEN_ANGLE = RealScalar.of(0.38196601125010515180);

  public static ColorDataIndexed of(int max, int sep) {
    Tensor tensor = Tensors.reserve(max * sep);
    Scalar offset = RationalScalar.of(2, 3);
    Tensor sats = Subdivide.of(1.0, 0.2, sep - 1);
    for (int index = 0; index < max; ++index) {
      for (Tensor sat : sats)
        tensor.append(ColorFormat.toVector(Hue.of(offset.number().doubleValue(), ((Scalar) sat).number().doubleValue(), 1.0, 1.0)));
      offset = MOD.apply(offset.add(GOLDEN_ANGLE));
    }
    return StrictColorDataIndexed.of(tensor);
  }
}
