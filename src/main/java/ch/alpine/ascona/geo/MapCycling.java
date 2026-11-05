// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFrame;

import ch.alpine.bridge.awt.LazyMouse;
import ch.alpine.bridge.awt.LazyMouseListener;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.awt.WindowClosed;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.ShowableConfig;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.geo.GeoComponent;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.io.FileBlock;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Drop;

@ReflectionMarker
class MapCycling implements ManipulateProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;
  public Color marker = Color.MAGENTA;
  public Color insert = Color.BLUE;
  public final RouteParam routeParam = new RouteParam();
  @FieldFuse
  public transient Boolean append = false;
  // ---
  final GeoComponent geoComponent = new GeoComponent() {
    @Override
    public void renderMore(Graphics2D graphics, ShowableConfig showableConfig) {
      RenderQuality.setQuality(graphics);
      RenderQuality.smoothLine(graphics, false);
      {
        Show show = Show.plain();
        for (RouteRecord seg : segments) {
          Showable showable = show.add(ListLinePlot.of(seg.path()));
          showable.setStroke(new BasicStroke(4));
          showable.setColor(marker);
        }
        show.showRender(graphics, showableConfig);
      }
      {
        Show show = Show.plain();
        {
          Showable showable = show.add(ListLinePlot.of(geoPath.pathSeq));
          showable.setStroke(new BasicStroke(4f));
          showable.setColor(insert);
        }
        {
          show.add(ListPlot.of(geoPath.pathSeq));
        }
        show.showRender(graphics, showableConfig);
      }
    };
  };
  private final List<RouteRecord> segments = StaticHelper.segments();
  GeoPath geoPath = new GeoPath();

  public MapCycling() {
    LazyMouseListener lml = new LazyMouseListener() {
      @Override
      public void lazyClicked(MouseEvent mouseEvent) {
        TilePixel tilePixel = geoComponent.fromPoint(mouseEvent.getPoint());
        int button = mouseEvent.getButton();
        switch (button) {
        case 1:
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
    if (append) {
      append = false;
      String line = routeParam.line(geoPath.pathSeq);
      IO.println(line);
      Path user_dir = Path.of(System.getProperty("user.dir"));
      Path path = user_dir.resolve("src/main/resources/ch/alpine/ascona/geo/2024_routes.csv");
      boolean exists = Files.exists(path);
      IO.println(exists);
      try (FileWriter fileWriter = new FileWriter(path.toFile(), true)) {
        fileWriter.append(line + "\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
      segments.add(routeParam.rec(geoPath.pathSeq));
      geoPath.pathSeq = Tensors.empty();
    }
    return geoComponent;
  }

  static void main() {
    ResourceLocator resourceLocator = ResourceLocator.of(MapCycling.class);
    IO.println(resourceLocator);
    if (!FileBlock.of(resourceLocator.resolve(""))) {
      MapCycling mapViewer = new MapCycling();
      Path properties = resourceLocator.properties(MapParam.class);
      MapParam mapParam = ObjectProperties.tryLoad(new MapParam(), properties);
      mapViewer.geoComponent.tilePixel = mapParam.prod();
      Path path = resourceLocator.resolve("francetours.properties");
      ObjectProperties.tryLoad(mapViewer.geoPath, path);
      JFrame jFrame = mapViewer.runStandalone();
      WindowClosed.runs(jFrame, () -> ObjectProperties.trySave(mapViewer.geoPath, path));
      WindowClosed.runs(jFrame, () -> {
        mapViewer.geoComponent.tileServer.cache().clearQueue();
        mapParam.read(mapViewer.geoComponent.tilePixel);
        ObjectProperties.trySave(mapParam, properties);
      });
    }
  }
}
