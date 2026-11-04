// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import ch.alpine.bridge.awt.LazyMouse;
import ch.alpine.bridge.awt.LazyMouseListener;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.awt.WindowClosed;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.ShowableConfig;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.geo.GeoComponent;
import ch.alpine.bridge.geo.GeoLayer;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.io.FileBlock;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.pro.ShowWindow;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.qty.Degree;
import ch.alpine.tensor.qty.QuantityMagnitude;
import ch.alpine.tensor.qty.UnitConvert;

/** For Java, I recommend an architecture based on
 * ExecutorService + BlockingQueue (or CompletableFuture) +
 * a thread-safe tile cache, with all GUI updates
 * dispatched to the GUI's event thread. */
@ReflectionMarker
class MapBoundaries implements ManipulateProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;
  public Boolean download = true;
  @FieldSelectionCallback("countries")
  public String country = "Spain";
  @FieldClip(min = "0", max = "0.01")
  @FieldSlider(showValue = true)
  public Scalar epsilon = RealScalar.of(0.01);
  public Integer depth = 6;
  @FieldFuse
  public transient Boolean load = false;

  public List<String> countries() {
    return GeoBoundaries.INSTANCE.countries();
  }

  // ---
  private final GeoComponent geoComponent = new GeoComponent() {
    @Override
    public void renderMore(GeoLayer geoLayer, Graphics2D graphics) {
      RenderQuality.setQuality(graphics);
      {
        Tensor paths = GeoBoundaries.INSTANCE.cache_apply(country, epsilon);
        if (Objects.nonNull(paths)) {
          Show show = Show.plain();
          for (Tensor path : paths) {
            Showable showable = show.add(PolygonPlot.of(path.maps(UnitConvert.SI().to(Degree.UNIT))));
            showable.setStroke(new BasicStroke(3));
            showable.setColor(new Color(255, 0, 0, 128 + 64));
          }
          ShowableConfig showableConfig = geoLayer.showableConfig();
          show.showRender(showableConfig).render(graphics);
        }
      }
      if (load) {
        new Thread(() -> {
          Tensor all = GeoBoundaries.INSTANCE.cache_apply(country, epsilon);
          if (Objects.nonNull(all)) {
            RegionTiles regionTiles = new RegionTiles(all, depth);
            {
              JDialog jDialog = ShowWindow.asDialog(regionTiles.shows());
              jDialog.setBounds(100, 100, 500, 500);
              SwingUtilities.invokeLater(() -> jDialog.setVisible(true));
            }
            if (download)
              regionTiles.stream().forEach(geoComponent.tileServer.cache()::getTile);
          }
        }).start();
        load = false;
      }
    };
  };

  public MapBoundaries() {
    LazyMouseListener lml = new LazyMouseListener() {
      @Override
      public void lazyClicked(MouseEvent mouseEvent) {
        int button = mouseEvent.getButton();
        switch (button) {
        case 1:
          TilePixel tilePixel = geoComponent.fromPoint(mouseEvent.getPoint());
          Tensor lon_lat = tilePixel.lon_lat();
          GeometryFactory gf = new GeometryFactory();
          ScalarUnaryOperator suo = QuantityMagnitude.SI().in("deg");
          Coordinate cd = new Coordinate( //
              suo.apply(lon_lat.Get(0)).number().doubleValue(), //
              suo.apply(lon_lat.Get(1)).number().doubleValue());
          Point point = gf.createPoint(cd);
          country = GeoBoundaries.INSTANCE.inside(point);
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
    return geoComponent;
  }

  static void main() {
    ResourceLocator resourceLocator = ResourceLocator.of(MapBoundaries.class);
    if (!FileBlock.of(resourceLocator.resolve(""))) {
      MapBoundaries mapViewer = new MapBoundaries();
      Path properties = resourceLocator.properties(MapParam.class);
      MapParam mapParam = ObjectProperties.tryLoad(new MapParam(), properties);
      mapViewer.geoComponent.tilePixel = mapParam.prod();
      JFrame jFrame = mapViewer.runStandalone();
      WindowClosed.runs(jFrame, () -> {
        mapViewer.geoComponent.tileServer.cache().clearQueue();
        mapParam.read(mapViewer.geoComponent.tilePixel);
        ObjectProperties.trySave(mapParam, properties);
      });
    }
  }
}
