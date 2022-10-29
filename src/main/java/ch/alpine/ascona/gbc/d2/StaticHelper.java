// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.LinearColorDataGradient;
import ch.alpine.tensor.itp.LinearBinaryAverage;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.N;

/* package */ enum StaticHelper {
  ;
  public static <T extends Tensor> Tensor of(CoordinateBoundingBox coordinateBoundingBox, ManifoldDisplay manifoldDisplay, int resolution) {
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1).map(N.DOUBLE);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1).map(N.DOUBLE);
    return Tensor.of(dy.stream().map(Scalar.class::cast).parallel() //
        .map(py -> Tensor.of(dx.stream().map(Scalar.class::cast) //
            .map(px -> Unprotect.using(List.of(px, py, RealScalar.ZERO))) //
            .map(manifoldDisplay::xya2point))));
  }

  public static ColorDataGradient custom(ColorDataGradients colorDataGradients, Clip clip, Scalar radius) {
    Tensor domain = Subdivide.increasing(clip, 50);
    Tensor rgba = Tensors.empty();
    IntBlend intBlend = new IntBlend(radius);
    Tensor c_blck = Tensors.vector(0, 0, 0, 255);
    for (int index = 0; index < domain.length(); ++index) {
      Scalar x = domain.Get(index);
      Tensor c_rgba = clip.rescale(x).map(colorDataGradients);
      Scalar weight = intBlend.apply(x);
      Tensor split = LinearBinaryAverage.INSTANCE.split(c_blck, c_rgba, weight);
      rgba.append(split);
    }
    return LinearColorDataGradient.of(rgba);
  }
}
