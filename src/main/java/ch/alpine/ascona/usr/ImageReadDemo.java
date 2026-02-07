// code by jph
package ch.alpine.ascona.usr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.Thumbnail;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.io.Import;

enum ImageReadDemo {
  ;
  static void main() throws IOException {
    // FIXME
    Path file = HomeDirectory.path("testimage", "COPY_P1150723_a.JPG");
    switch (2) {
    case 1: {
      try (InputStream inputStream = Files.newInputStream(file)) {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        System.out.println(bufferedImage);
        Tensor tensor = ImageFormat.from(bufferedImage);
        Tensor thumb = Thumbnail.of(tensor, 100);
        Export.of(HomeDirectory.path("testimage", "thumb.jpg"), thumb);
      }
      break;
    }
    case 2: {
      Tensor tensor2 = Import.of(file);
      Tensor thumb = Thumbnail.of(tensor2, 200);
      Export.of(HomeDirectory.path("testimage", "thumb2.jpg"), thumb);
      break;
    }
    default:
      throw new IllegalArgumentException();
    }
  }
}
