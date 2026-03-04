// code by jph
package ch.alpine.ascona.usr;

import java.util.concurrent.TimeUnit;

import ch.alpine.ascony.io.AnimationWriter;
import ch.alpine.ascony.io.GifAnimationWriter;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.d.DiscreteUniformDistribution;

/* package */ enum AnimationWriterDemo {
  ;
  static void main() throws Exception {
    try (AnimationWriter animationWriter = //
        new GifAnimationWriter(HomeDirectory.Pictures.resolve("grayscale.gif"), 100, TimeUnit.MILLISECONDS)) {
      for (int count = 1; count <= 16; ++count) {
        Distribution distribution = DiscreteUniformDistribution.forArray(count * 16);
        animationWriter.write(RandomVariate.of(distribution, 128, 128));
      }
    }
    try (AnimationWriter animationWriter = //
        new GifAnimationWriter(HomeDirectory.Pictures.resolve("colornoise.gif"), 100, TimeUnit.MILLISECONDS)) {
      for (int count = 1; count <= 16; ++count) {
        Distribution distribution = DiscreteUniformDistribution.forArray(count * 16);
        Tensor image = RandomVariate.of(distribution, 128, 128, 4);
        image.set(_ -> RealScalar.of(255), Tensor.ALL, Tensor.ALL, 3);
        animationWriter.write(image);
      }
    }
    try (AnimationWriter animationWriter = //
        new GifAnimationWriter(HomeDirectory.Pictures.resolve("palettenoise.gif"), 100, TimeUnit.MILLISECONDS)) {
      Distribution distribution = DiscreteUniformDistribution.forArray(256);
      for (int count = 1; count <= 16; ++count)
        animationWriter.write(Raster.of(RandomVariate.of(distribution, 128, 128), ColorDataGradients.ALPINE));
    }
  }
}
