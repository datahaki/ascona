// code by jph
package ch.alpine.ascona.usr;

import java.awt.Container;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.so.So3Exponential;
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

@ReflectionMarker
/* package */ enum So3ExponentialDemo implements ManipulateProvider {
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
    Path path = HomeDirectory.Pictures.resolve("rodriquez.gif");
    try (AnimationWriter animationWriter = //
        new GifAnimationWriter(path, millis, TimeUnit.MILLISECONDS)) {
      for (Tensor _z : Subdivide.of(-4 * Math.PI, 4 * Math.PI, 40)) {
        System.out.println(_z);
        Slice slice = new Slice((Scalar) _z);
        Tensor matrix = Parallelize.matrix(slice::function, RES, RES);
        animationWriter.write(Raster.of(matrix, ColorDataGradients.CLASSIC));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    ImageIcon imageIcon = null;
    try {
      imageIcon = new ImageIcon(path.toUri().toURL());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return new JLabel(imageIcon);
  }

  static void main() {
    INSTANCE.runStandalone();
  }
}
