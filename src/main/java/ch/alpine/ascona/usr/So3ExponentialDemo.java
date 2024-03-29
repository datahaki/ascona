// code by jph
package ch.alpine.ascona.usr;

import java.util.concurrent.TimeUnit;

import ch.alpine.sophus.lie.so3.Rodrigues;
import ch.alpine.tensor.Parallelize;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;
import ch.alpine.tensor.io.AnimationWriter;
import ch.alpine.tensor.io.GifAnimationWriter;

/* package */ enum So3ExponentialDemo {
  ;
  private static final int RES = 192;
  private static final Tensor RE = Subdivide.of(-4, +4, RES - 1);
  private static final Tensor IM = Subdivide.of(-4, +4, RES - 1);
  private static Scalar Z;

  private static Scalar function(int y, int x) {
    Tensor mat = Rodrigues.INSTANCE.exp(Tensors.of(RE.Get(x), IM.Get(y), Z));
    return mat.Get(0, 2);
  }

  public static void main(String[] args) throws Exception {
    try (AnimationWriter animationWriter = //
        new GifAnimationWriter(HomeDirectory.Pictures("rodriquez.gif"), 100, TimeUnit.MILLISECONDS)) {
      for (Tensor _z : Subdivide.of(-4 * Math.PI, 4 * Math.PI, 40)) {
        System.out.println(_z);
        Z = (Scalar) _z;
        Tensor matrix = Parallelize.matrix(So3ExponentialDemo::function, RES, RES);
        animationWriter.write(Raster.of(matrix, ColorDataGradients.CLASSIC));
      }
    }
  }
}
