// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JFrame;

import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.awt.WindowClosed;
import ch.alpine.bridge.geo.GeoComponent;
import ch.alpine.bridge.geo.GeoLayer;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.io.FileBlock;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.sophis.crv.d2.alg.PolygonClip;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;

/** For Java, I recommend an architecture based on
 * ExecutorService + BlockingQueue (or CompletableFuture) +
 * a thread-safe tile cache, with all GUI updates
 * dispatched to the GUI's event thread. */
@ReflectionMarker
class MapBoundaries implements ManipulateProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;
  public Boolean debug_print = true;
  public Boolean download = true;
  public Boolean availability = true;
  @FieldSelectionCallback("countries")
  public String country = "Spain";
  @FieldClip(min = "0", max = "0.01")
  @FieldSlider
  public Scalar epsilon = RealScalar.of(0.01);
  public Integer depth = 6;
  @FieldFuse
  public transient Boolean load = false;

  public List<String> countries() {
    return GeoBoundaries.INSTACE.countries();
  }

  // ---
  private final GeoComponent geoComponent = new GeoComponent() {
    @Override
    public void renderMore(GeoLayer geoLayer, Graphics2D graphics) {
      RenderQuality.setQuality(graphics);
      Dimension dimension = getSize();
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
        Boundary boundary = new Boundary(country, epsilon);
        Tensor paths = GeoBoundaries.INSTACE.cache.apply(boundary);
        if (Objects.nonNull(paths))
          for (Tensor path : paths) {
            Tensor result = path;
            Path2D path2d = geoLayer.toPath2D(result);
            graphics.setColor(new Color(255, 0, 0, 64));
            graphics.fill(path2d);
            graphics.setColor(Color.RED);
            graphics.setStroke(new BasicStroke());
            graphics.draw(path2d);
          }
      }
      if (load) {
        load();
        load = false;
      }
    };
  };

  public void load() {
    Boundary boundary = new Boundary(country, epsilon);
    Tensor all = GeoBoundaries.INSTACE.cache.apply(boundary);
    if (Objects.nonNull(all)) {
      List<Tile> prev0 = new LinkedList<>();
      List<Tile> prev1 = new LinkedList<>();
      prev0.add(new Tile(0, 0, 0));
      for (int z = 0; z < depth; ++z) {
        IO.println(z + " " + prev0.size() + " " + prev1.size());
        List<Tile> next0 = new LinkedList<>();
        List<Tile> next1 = new LinkedList<>();
        for (Tile tile : prev0) {
          Tensor cbb_lat_lon = Reverse.of(tile.cbb_lat_lon());
          TensorUnaryOperator tuo = PolygonClip.of(cbb_lat_lon);
          CoordinateBoundingBox cbb0 = CoordinateBounds.of(cbb_lat_lon);
          for (Tensor region : all) {
            Tensor tensor = tuo.apply(region);
            if (!Tensors.isEmpty(tensor)) {
              CoordinateBoundingBox cbb1 = CoordinateBounds.of(tensor);
              boolean exact = cbb0.equals(cbb1);
              if (exact) {
                next1.addAll(tile.children());
              } else
                next0.addAll(tile.children());
              break;
            }
          }
        }
        for (Tile tile : prev1)
          next1.addAll(tile.children());
        prev0 = next0;
        prev1 = next1;
        if (download) {
          MapImagesCache mapImagesCache = geoComponent.tileServer.cache();
          List<Tile> list = Stream.concat(prev0.stream(), prev1.stream()).toList();
          list.forEach(mapImagesCache::getTile);
        }
      }
    }
  }

  @Override
  public Container getContainer() {
    geoComponent.tileServer = tileServers;
    // geoComponent.getCache().debug_print = debug_print;
    return geoComponent;
  }

  static void main() {
    ResourceLocator resourceLocator = ResourceLocator.of(MapBoundaries.class);
    if (!FileBlock.of(resourceLocator.resolve(""))) {
      MapBoundaries mapViewer = new MapBoundaries();
      Path properties = resourceLocator.properties(MapParam.class);
      {
        MapParam mapParam = ObjectProperties.tryLoad(new MapParam(), properties);
        long[] array = Primitives.toLongArray(mapParam.center);
        mapViewer.geoComponent.tilePixel = TilePixel.of((int) array[0], array[1], array[2]);
      }
      // Path path = resourceLocator.resolve("region.properties");
      // ObjectProperties.tryLoad(mapViewer.geoPath, path);
      JFrame jFrame = mapViewer.runStandalone();
      // WindowClosed.runs(jFrame, () -> ObjectProperties.trySave(mapViewer.geoPath, path));

      WindowClosed.runs(jFrame, () -> {
        MapParam mapParam = new MapParam();
        mapViewer.geoComponent.tileServer.cache().clearQueue();
        TilePixel tilePixel = mapViewer.geoComponent.tilePixel;
        mapParam.center = Tensors.vector(tilePixel.tile().z(), tilePixel.absX(), tilePixel.absY());
        ObjectProperties.trySave(mapParam, properties);
      });
    }
  }
}
