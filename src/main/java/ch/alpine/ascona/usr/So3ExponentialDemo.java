// code by jph
package ch.alpine.ascona.usr;

import java.awt.Container;
import java.time.Duration;

import ch.alpine.bridge.awt.AwtUtil;
import ch.alpine.bridge.io.ani.ImageIconRecorder;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.so.So3Exponential;
import ch.alpine.tensor.Parallelize;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;

@ReflectionMarker
enum So3ExponentialDemo implements ManipulateProvider {
  INSTANCE;

  private static final int RES = 192;
  private static final Tensor RE = Subdivide.of(-4, +4, RES - 1);
  private static final Tensor IM = Subdivide.of(-4, +4, RES - 1);
  @FieldClip(min = "10", max = "1000")
  public Integer millis = 100;

  record Slice(Scalar Z) {
    Scalar function(int y, int x) {
      Tensor mat = So3Exponential.vectorExp(Tensors.of(RE.Get(x), IM.Get(y), Z));
      return mat.Get(0, 2);
    }
  }

  @Override
  public Container getContainer() {
    ImageIconRecorder imageIconRecorder = ImageIconRecorder.loop(Duration.ofMillis(millis));
    for (Tensor _z : Subdivide.of(-4 * Math.PI, 4 * Math.PI, 10)) {
      IO.print(".");
      Slice slice = new Slice((Scalar) _z);
      Tensor matrix = Parallelize.matrix(slice::function, RES, RES);
      imageIconRecorder.write(Raster.of(matrix, ColorDataGradients.CLASSIC));
    }
    return AwtUtil.iconAsLabel(imageIconRecorder.getIconImage());
  }

  static void main() {
    // TODO ASCONA when value was changed... than window close launches listener again!
    // ... this is probably intentional e.g. in order to ensure saving params
    INSTANCE.runStandalone();
  }
}
