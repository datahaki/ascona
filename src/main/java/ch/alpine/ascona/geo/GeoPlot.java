// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.IntStream;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;

@ReflectionMarker
class GeoPlot implements ManipulateProvider {
  static final Tensor segments = StaticHelper.segments();
  public TileServers ts = TileServers.OpenStreetMap;
  @FieldSelectionCallback("select")
  public transient Integer index = 0;
  public Color marker = Color.MAGENTA;

  public List<Integer> select() {
    return IntStream.range(Math.max(0, index - 5), Math.min(index + 6, segments.length())).boxed().toList();
  }

  public static void load(int index, TileServers ts) {
    Tensor seg = segments.get(index);
    Tensor ll0 = seg.get(0);
    Tensor ll1 = seg.get(1);
    int z = 0;
    for (z = 3; z < 18; ++z) {
      TilePixel pr = TilePixel.from(z, ll0);
      TilePixel qr = TilePixel.from(z, ll1);
      long dx = Math.abs(pr.absX() - qr.absX());
      long dy = Math.abs(pr.absY() - qr.absY());
      long max = Math.max(dx, dy);
      if (512 < max / 4)
        break;
      TilePixel p = TilePixel.from(z, ll0);
      TilePixel q = TilePixel.from(z, ll1);
      int px = p.tile().x();
      int qx = q.tile().x();
      int py = p.tile().y();
      int qy = q.tile().y();
      MapImagesCache mapImagesCache = ts.cache();
      int x0 = Math.min(px, qx) - 1;
      int x1 = Math.max(px, qx) + 1;
      int y0 = Math.min(py, qy) - 1;
      int y1 = Math.max(py, qy) + 1;
      int xl = x1 - x0 + 1;
      int yl = y1 - y0 + 1;
      if (xl * yl <= 100)
        for (int x = x0; x <= x1; ++x) {
          for (int y = y0; y <= y1; ++y) {
            Tile tile = new Tile(z, x, y);
            mapImagesCache.getTile(tile);
          }
        }
      else
        System.err.println(index);
    }
  }

  @Override
  public Container getContainer() {
    Tensor seg = segments.get(index);
    Tensor ll0 = seg.get(0);
    Tensor ll1 = seg.get(1);
    int z = 0;
    for (z = 0; z < 18; ++z) {
      TilePixel p = TilePixel.from(z, ll0);
      TilePixel q = TilePixel.from(z, ll1);
      long dx = Math.abs(p.absX() - q.absX());
      long dy = Math.abs(p.absY() - q.absY());
      long max = Math.max(dx, dy);
      if (512 < max)
        break;
    }
    --z;
    --z;
    --z;
    TilePixel p = TilePixel.from(z, ll0);
    TilePixel q = TilePixel.from(z, ll1);
    int px = p.tile().x();
    int qx = q.tile().x();
    int py = p.tile().y();
    int qy = q.tile().y();
    MapImagesCache mapImagesCache = ts.cache();
    int x0 = Math.min(px, qx) - 1;
    int x1 = Math.max(px, qx) + 1;
    int y0 = Math.min(py, qy) - 1;
    int y1 = Math.max(py, qy) + 1;
    int xl = x1 - x0 + 1;
    int yl = y1 - y0 + 1;
    BufferedImage bufferedImage = new BufferedImage(xl << 8, yl << 8, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    if (xl * yl <= 100)
      for (int x = x0; x <= x1; ++x) {
        for (int y = y0; y <= y1; ++y) {
          Tile tile = new Tile(z, x, y);
          graphics.drawImage(mapImagesCache.getTile(tile), (x - x0) << 8, (y - y0) << 8, null);
        }
      }
    else
      System.err.println(index);
    TilePixel origin = new TilePixel(new Tile(z, x0, y0), 0, 0);
    {
      graphics.setColor(marker);
      graphics.setStroke(new BasicStroke(4f));
      {
        TilePixel beg = origin.from(seg.get(0));
        TilePixel end = origin.from(seg.get(1));
        int p1x = (int) (beg.absX() - origin.absX());
        int p1y = (int) (beg.absY() - origin.absY());
        int p2x = (int) (end.absX() - origin.absX());
        int p2y = (int) (end.absY() - origin.absY());
        graphics.drawLine(p1x, p1y, p2x, p2y);
      }
    }
    graphics.dispose();
    Show show = new Show();
    show.add(ImagePlot.of(bufferedImage));
    return ShowGridComponent.of(show);
  }

  static void main() {
    new GeoPlot().runStandalone();
  }
}
