// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import ch.alpine.ascony.dis.R2Display;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataGradients;

/* package */ enum HilbertCoordinateShow {
  ;
  static void main() throws IOException {
    for (int n = 2; n < 5; ++n) {
      System.out.println(n);
      Tensor sequence = HilbertBenchmarkDemo.unit(n);
      BufferedImage bufferedImage = HilbertLevelImage.of( //
          R2Display.INSTANCE, sequence, 60, ColorDataGradients.CLASSIC, 800);
      Path path = HomeDirectory.Pictures.resolve(String.format("hc%d.png", n));
      try (OutputStream outputStream = Files.newOutputStream(path)) {
        ImageIO.write(bufferedImage, "png", outputStream);
      }
    }
  }
}
