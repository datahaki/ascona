// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JDialog;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.pro.ShowWindow;

enum PlotAvailable {
  ;
  static void main() {
    MapImagesCache mapImagesCache = TileServers.OpenStreetMap.cache();
    mapImagesCache.debug_print = true;
    final int zpix = 4;
    BufferedImage bufferedImage = MapOffscreen.world(zpix);
    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setColor(new Color(255, 0, 0, 128));
    graphics.setStroke(new BasicStroke());
    int delta = 8;
    final int zmax = zpix + delta;
    int wid = 256 >> delta;
    long counted = 0;
    int max = Tile.maxInclusive(zmax);
    for (int x = 0; x <= max; ++x)
      for (int y = 0; y <= max; ++y) {
        Tile tile = new Tile(zmax, x, y);
        boolean isAvailable = mapImagesCache.isAvailableOffline(tile);
        if (!isAvailable) {
          graphics.fillRect(x * wid, y * wid, wid, wid);
          ++counted;
        }
      }
    IO.println("missing: " + counted);
    graphics.dispose();
    Show show = new Show();
    show.add(ImagePlot.of(bufferedImage));
    JDialog jDialog = ShowWindow.asDialog(List.of(show));
    jDialog.setBounds(50, 50, 1000, 1000);
    jDialog.setVisible(true);
  }
}
