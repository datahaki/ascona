// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.nio.file.Path;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import ch.alpine.bridge.awt.LazyMouse;
import ch.alpine.bridge.awt.LazyMouseListener;
import ch.alpine.bridge.fig.PlotOption;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.ShowableConfig;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.geo.GeoComponent;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.pro.ShowWindow;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.sca.Round;

/** For Java, I recommend an architecture based on
 * ExecutorService + BlockingQueue (or CompletableFuture) +
 * a thread-safe tile cache, with all GUI updates
 * dispatched to the GUI's event thread. */
@ReflectionMarker
class MapRegion implements ManipulateProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;
  @FieldFuse
  public transient Boolean pathClear = false;
  public Integer depth = 13;
  public Boolean download = false;
  @FieldFuse
  public transient Boolean load = false;
  @FieldFuse
  public transient Boolean print = false;
  // ---
  private final GeoComponent geoComponent = new GeoComponent() {
    @Override
    public void renderMore(Graphics2D graphics, ShowableConfig showableConfig) {
      Show show = Show.plain();
      {
        Showable showable = show.add(PolygonPlot.of(geoPath.pathSeq, PlotOption.FILL));
        showable.setStroke(new BasicStroke(4f));
        showable.setColor(new Color(255, 0, 0, 32));
      }
      {
        show.add(ListPlot.of(geoPath.pathSeq));
      }
      show.showRender(graphics, showableConfig);
    };
  };
  private GeoPath geoPath = new GeoPath();
  ResourceLocator resourceLocator = ResourceLocator.of(MapRegion.class);
  Path properties = resourceLocator.properties(MapParam.class);
  Path path = resourceLocator.resolve("region2.properties");
  MapParam mapParam = ObjectProperties.tryLoad(new MapParam(), properties);

  public MapRegion() {
    geoComponent.setTilePixel(mapParam.prod());
    ObjectProperties.tryLoad(geoPath, path);
    LazyMouseListener lml = new LazyMouseListener() {
      @Override
      public void lazyClicked(MouseEvent mouseEvent) {
        int button = mouseEvent.getButton();
        switch (button) {
        case 1:
          TilePixel tilePixel = geoComponent.fromPoint(mouseEvent.getPoint());
          geoPath.pathSeq.append(tilePixel.lon_lat());
          geoComponent.repaint();
          break;
        case 3:
          if (0 < geoPath.pathSeq.length()) {
            geoPath.pathSeq = Drop.tail(geoPath.pathSeq, 1);
            geoComponent.repaint();
          }
          break;
        }
      }
    };
    new LazyMouse(lml).addListenersTo(geoComponent);
  }

  @Override
  public Container getContainer() {
    geoComponent.tileServer = tileServers;
    if (pathClear) {
      geoPath.pathSeq = Tensors.empty();
      pathClear = false;
    }
    if (load) {
      new Thread(() -> {
        Tensor all = Tensors.of(geoPath.pathSeq);
        RegionTiles regionTiles = new RegionTiles(all, depth);
        {
          JDialog jDialog = ShowWindow.asDialog(regionTiles.shows());
          jDialog.setBounds(100, 100, 500, 500);
          SwingUtilities.invokeLater(() -> jDialog.setVisible(true));
        }
        if (download)
          regionTiles.stream().forEach(geoComponent.tileServer.cache()::getTile);
      }).start();
      load = false;
    }
    if (print) {
      IO.println(geoPath.pathSeq.maps(Round._6));
      print = false;
    }
    return geoComponent;
  }

  @Override
  public void windowClosed() {
    ObjectProperties.trySave(geoPath, path);
    geoComponent.tileServer.cache().clearQueue();
    mapParam.read(geoComponent.getTilePixel());
    ObjectProperties.trySave(mapParam, properties);
  }

  static void main() {
    new MapRegion().runStandalone();
  }
}
