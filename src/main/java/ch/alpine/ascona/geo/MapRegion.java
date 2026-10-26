// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.swing.JDialog;
import javax.swing.JFrame;

import ch.alpine.bridge.awt.AwtUtil;
import ch.alpine.bridge.awt.LazyMouse;
import ch.alpine.bridge.awt.LazyMouseListener;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.awt.WindowClosed;
import ch.alpine.bridge.fig.PlotOption;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.geo.GeoComponent;
import ch.alpine.bridge.geo.GeoLayer;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.gfx.TextContour;
import ch.alpine.bridge.io.FileBlock;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.pro.ShowWindow;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.sophis.crv.d2.alg.PolygonClip;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Round;

/** For Java, I recommend an architecture based on
 * ExecutorService + BlockingQueue (or CompletableFuture) +
 * a thread-safe tile cache, with all GUI updates
 * dispatched to the GUI's event thread. */
@ReflectionMarker
class MapRegion implements ManipulateProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;
  public Boolean debug_print = true;
  public Boolean download = true;
  public Boolean availability = true;
  @FieldFuse
  public transient Boolean pathClear = false;
  public Integer depth = 13;
  @FieldFuse
  public transient Boolean load = false;
  // ---
  private final GeoComponent geoComponent = new GeoComponent() {
    @Override
    public void renderMore(GeoLayer geoLayer, Graphics2D graphics) {
      RenderQuality.setQuality(graphics);
      RenderQuality.smoothLine(graphics, false);
      TextContour textContour = TextContour.of(graphics);
      Dimension dimension = getSize();
      final Point center = AwtUtil.center(dimension);
      if (availability) {
        MapImagesCache mapImagesCache = tileServer.cache();
        // IO.println("ZERO " + geoLayer.origin());
        for (int iz = 1; iz <= 1; iz++) {
          Optional<TilePixel> optional = geoLayer.origin().zoomIncr(iz, tileServer.z_max());
          if (optional.isPresent()) {
            TilePixel zoom = optional.orElseThrow();
            graphics.setColor(new Color(128, 0, 128, 16));
            int fac = 1 << iz;
            for (int ix = 0; ix < dimension.width * fac + 256; ix += 256)
              for (int iy = 0; iy < dimension.height * fac + 256; iy += 256) {
                TilePixel shift = zoom.shift(ix, iy);
                if (!mapImagesCache.isAvailableOffline(shift.tile())) {
                  graphics.fillRect((ix - shift.pix() + 128) / fac - 4, (iy - shift.piy() + 128) / fac - 4, 8, 8);
                }
                // BufferedImage bufferedImage = mapImagesCache.getTile(shift.tile());
                // graphics.drawImage(bufferedImage, (ix - shift.pix()) / 2, (iy - shift.piy()) / 2, 128, 128, null);
              }
          }
        }
      }
      {
        graphics.setStroke(new BasicStroke(2f));
        Path2D path2d = geoLayer.toPath2D(geoPath.pathSeq);
        graphics.setColor(Color.RED);
        graphics.draw(path2d);
        textContour.draw("" + geoPath.distance().maps(Round.FUNCTION), 0, 50);
        if (0 < geoPath.pathSeq.length()) {
          graphics.setColor(new Color(255, 0, 0, 32));
          graphics.fill(path2d);
        }
      }
    };
  };
  private GeoPath geoPath = new GeoPath();

  public MapRegion() {
    LazyMouseListener lml = new LazyMouseListener() {
      @Override
      public void lazyClicked(MouseEvent mouseEvent) {
        int button = mouseEvent.getButton();
        switch (button) {
        case 1:
          TilePixel tilePixel = geoComponent.fromPoint(mouseEvent.getPoint());
          geoPath.pathSeq.append(tilePixel.lat_lon());
          geoComponent.repaint();
          break;
        }
      }
    };
    new LazyMouse(lml).addListenersTo(geoComponent);
  }

  @Override
  public Container getContainer() {
    geoComponent.tileServer = tileServers;
    // geoComponent.getCache().debug_print = debug_print;
    if (pathClear) {
      if (Tensors.nonEmpty(geoPath.pathSeq))
        geoPath.pathSeq = Drop.tail(geoPath.pathSeq, 1);
      pathClear = false;
    }
    if (load) {
      // ObjectProperties.trySave(mapViewer.geoPath, path);
      Tensor region = Reverse.of(geoPath.pathSeq);
      CoordinateBoundingBox cbb = CoordinateBounds.of(geoPath.pathSeq);
      Clip clip0 = cbb.clip(0);
      Clip clip1 = cbb.clip(1);
      List<Show> list = new LinkedList<Show>();
      // IO.println("START " + beg);
      int z_start = geoComponent.tilePixel.tile().z();
      for (int z = z_start; z <= Math.min(depth, geoComponent.tileServer.z_max()); ++z) {
        Show show = new Show();
        show.add(PolygonPlot.of(region));
        TilePixel beg = TilePixel.from(z, clip0.max(), clip1.min());
        TilePixel end = TilePixel.from(z, clip0.min(), clip1.max());
        for (int x = beg.tile().x(); x <= end.tile().x(); ++x) {
          for (int y = beg.tile().y(); y <= end.tile().y(); ++y) {
            Tile tile = new Tile(z, x, y);
            Tensor cbb_lat_lon = Reverse.of(tile.cbb_lat_lon());
            show.add(PolygonPlot.of(cbb_lat_lon));
            TensorUnaryOperator tuo = PolygonClip.of(cbb_lat_lon);
            Tensor tensor = tuo.apply(region);
            show.add(PolygonPlot.of(tensor, PlotOption.FILL));
            MapImagesCache mapImagesCache = geoComponent.getCache();
            if (!mapImagesCache.isAvailableOffline(tile))
              mapImagesCache.getTile(tile);
          }
        }
        list.add(show);
      }
      load = false;
      {
        JDialog jDialog = ShowWindow.asDialog(list);
        jDialog.setBounds(100, 100, 500, 500);
        jDialog.setVisible(true);
      }
    }
    return geoComponent;
  }

  static void main() {
    ResourceLocator resourceLocator = ResourceLocator.of(MapRegion.class);
    if (!FileBlock.of(resourceLocator.resolve(""))) {
      MapRegion mapViewer = new MapRegion();
      Path properties = resourceLocator.properties(MapParam.class);
      {
        MapParam mapParam = ObjectProperties.tryLoad(new MapParam(), properties);
        long[] array = Primitives.toLongArray(mapParam.center);
        mapViewer.geoComponent.tilePixel = TilePixel.of((int) array[0], array[1], array[2]);
      }
      Path path = resourceLocator.resolve("region.properties");
      ObjectProperties.tryLoad(mapViewer.geoPath, path);
      JFrame jFrame = mapViewer.runStandalone();
      WindowClosed.runs(jFrame, () -> ObjectProperties.trySave(mapViewer.geoPath, path));
      WindowClosed.runs(jFrame, () -> {
        MapParam mapParam = new MapParam();
        TilePixel tilePixel = mapViewer.geoComponent.tilePixel;
        mapParam.center = Tensors.vector(tilePixel.tile().z(), tilePixel.absX(), tilePixel.absY());
        ObjectProperties.trySave(mapParam, properties);
      });
    }
  }
}
