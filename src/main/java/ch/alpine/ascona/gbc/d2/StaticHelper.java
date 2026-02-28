// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.LinearColorDataGradient;
import ch.alpine.tensor.itp.LinearBinaryAverage;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Round;

/* package */ enum StaticHelper {
  ;
  static record IntBlend(Scalar radius) implements ScalarUnaryOperator {
    @Override
    public Scalar apply(Scalar scalar) {
      return Min.of(Abs.between(scalar, Round.FUNCTION.apply(scalar)).divide(radius), RealScalar.ONE);
    }
  }

  public static ColorDataGradient custom(ColorDataGradients colorDataGradients, Clip clip, Scalar radius) {
    Tensor domain = Subdivide.increasing(clip, 50);
    Tensor rgba = Tensors.empty();
    IntBlend intBlend = new IntBlend(radius);
    Tensor c_blck = Tensors.vector(0, 0, 0, 255);
    for (int index = 0; index < domain.length(); ++index) {
      Scalar x = domain.Get(index);
      Tensor c_rgba = clip.rescale(x).maps(colorDataGradients);
      Scalar weight = intBlend.apply(x);
      Tensor split = LinearBinaryAverage.INSTANCE.split(c_blck, c_rgba, weight);
      rgba.append(split);
    }
    return LinearColorDataGradient.of(rgba);
  }
}
