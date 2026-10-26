// code by jph
package ch.alpine.ascona.geo;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.alpine.bridge.awt.OffscreenRender;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TileServers;
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

  static BufferedImage world(int z) {
    int wid = 1 << (z + 8);
    BufferedImage bufferedImage = new BufferedImage(wid, wid, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    MapImagesCache mapImagesCache = TileServers.OpenStreetMap.cache();
    // mapImagesCache.debug_print = true;
    int max = Tile.maxInclusive(z);
    for (int x = 0; x <= max; ++x)
      for (int y = 0; y <= max; ++y) {
        Tile tile = new Tile(z, x, y);
        BufferedImage image = mapImagesCache.getTile(tile).bufferedImage();
        graphics.drawImage(image, x << 8, y << 8, null);
      }
    graphics.dispose();
    return bufferedImage;
  }
}
