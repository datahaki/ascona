// code by jph
package ch.alpine.ascona.geo;

import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.alpine.bridge.awt.OffscreenRender;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.tensor.ext.HomeDirectory;

enum MapOffscreen {
  ;
  static void main() throws IOException {
    ManipulateProvider manipulateProvider = new MapViewer();
    Container jComponent = manipulateProvider.getContainer();
    int width = 4096;
    int height = 4096;
    jComponent.setSize(width, height);
    BufferedImage bufferedImage = OffscreenRender.of(jComponent);
    ImageIO.write(bufferedImage, "png", HomeDirectory.Pictures.resolve("iberico2.png").toFile());
  }
}
