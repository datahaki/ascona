// code by jph
package ch.alpine.ascona.geo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.math.Unitless;
import ch.alpine.bridge.pro.ShowProvider;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.red.Mean;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

enum TileDiscovery {
  ;
  static BufferedImage here(List<Tile> list, Set<Tile> missing) {
    int z = list.getFirst().z();
    int width = Tile.maxExclusive(z);
    BufferedImage bufferedImage = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width, width);
    graphics.setColor(Color.GREEN);
    for (Tile tile : list) {
      Integers.requireEquals(z, tile.z());
      graphics.fillRect(tile.x(), tile.y(), 1, 1);
    }
    graphics.setColor(Color.RED);
    for (Tile tile : missing) {
      Integers.requireEquals(z, tile.z());
      graphics.fillRect(tile.x(), tile.y(), 1, 1);
    }
    graphics.dispose();
    return bufferedImage;
  }

  static void main() {
    MapImagesCache mapImagesCache = TileServers.OpenStreetMap.cache();
    List<Tile> prev = List.of(new Tile(0, 0, 0));
    while (prev.getFirst().z() <= 12) {
      Collections.shuffle(prev);
      Set<Tile> missing = new HashSet<>();
      for (Tile tile : prev) {
        Path path = mapImagesCache.path(tile);
        if (!Files.exists(path)) {
          mapImagesCache.getTile(tile);
          missing.add(tile);
        }
      }
      if (!missing.isEmpty()) {
        BufferedImage bufferedImage = here(prev, missing);
        String label = "missing: " + Round._2.apply(Unitless.PERCENT.unitConvert().apply(Rational.of(missing.size(), prev.size())));
        ShowProvider showProvider = new ShowProvider() {
          @Override
          public Show getShow() {
            Show show = new Show();
            show.add(ImagePlot.of(bufferedImage));
            show.setShowLabel(label);
            return show;
          }
        };
        SwingUtilities.invokeLater(() -> showProvider.runStandalone());
      }
      mapImagesCache.blockUntilDone();
      List<Tile> next = new ArrayList<>(prev.size() * 4);
      Clip absolute = Clips.absolute(Unitless.DEGREES.of(65));
      for (Tile tile : prev) {
        Path path = mapImagesCache.path(tile);
        try {
          long size = Files.size(path);
          if (size > 200) {
            Tensor lat_lon = Mean.of(tile.cbb());
            Scalar lon = lat_lon.Get(1);
            if (absolute.isInside(lon))
              next.addAll(tile.children());
          } else //
          if (size < 100) {
            IO.println("deleting " + size + " " + path);
            Files.delete(path);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      prev = next;
    }
  }
}
