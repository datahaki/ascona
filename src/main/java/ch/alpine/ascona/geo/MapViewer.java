// code by jph
package ch.alpine.ascona.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import javax.swing.JFrame;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.S2Display;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.awt.AwtUtil;
import ch.alpine.bridge.awt.LazyMouse;
import ch.alpine.bridge.awt.LazyMouseListener;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.awt.WindowClosed;
import ch.alpine.bridge.fig.Ticks;
import ch.alpine.bridge.geo.GeoComponent;
import ch.alpine.bridge.geo.GeoLayer;
import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.TextContour;
import ch.alpine.bridge.io.FileBlock;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.sophis.api.GeoPosition;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.opt.nd.BoxRandomSample;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.UnitConvert;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

@ReflectionMarker
class MapViewer implements ManipulateProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;
  public Boolean debug_print = true;
  public Boolean download = true;
  public Boolean crosshair = true;
  public Boolean ticks = true;
  public Boolean gridlines = true;
  public Color gridLineCol = new Color(128, 128, 128, 64);
  public Boolean availability = true;
  public Boolean showCycles = false;
  public Boolean showPoi = false;
  public Boolean showGlobe = false;
  @FieldSlider
  @FieldClip(min = "0", max = "5")
  public Integer minizoom = 3;
  @FieldSelectionArray({ "128", "256", "384", "512" })
  public Integer minilen = 256;
  public Color marker = Color.MAGENTA;
  public Integer depth = 13;
  @FieldFuse
  public transient Boolean stats = false;
  public String pathName = "glbe";
  public Boolean pathAdd = false;
  @FieldFuse
  public transient Boolean pathClear = false;
  // ---
  private final GeoComponent geoComponent = new GeoComponent() {
    @Override
    public void renderMore(GeoLayer geoLayer, Graphics2D graphics) {
      RenderQuality.setQuality(graphics);
      RenderQuality.smoothLine(graphics, false);
      TextContour textContour = TextContour.of(graphics);
      Dimension dimension = getSize();
      final Point center = AwtUtil.center(dimension);
      /* upper left corner */
      if (crosshair) { // draw crosshair
        graphics.setStroke(new BasicStroke());
        graphics.setColor(new Color(255, 0, 0, 192));
        int r = 3;
        graphics.drawLine(center.x - r, center.y, center.x + r, center.y);
        graphics.drawLine(center.x, center.y - r, center.x, center.y + r);
        textContour.draw("z=" + tilePixel.tile().z(), 2, 20);
      }
      if (ticks && 2 < tilePixel.tile().z()) {
        ScalarUnaryOperator suo = UnitConvert.SI().to("deg");
        FontMetrics fontMetrics = graphics.getFontMetrics();
        { // lat
          Scalar max = suo.apply(tilePixel.shift(0, -center.y).lat_lon().Get(0));
          Scalar min = suo.apply(tilePixel.shift(0, +center.y).lat_lon().Get(0));
          while (Scalars.lessThan(max, min))
            max = max.add(Quantity.of(180, "deg"));
          Clip clip = Clips.interval(min, max);
          List<Scalar> list = Ticks.stream(clip, Rational.of(100, dimension.height)).toList();
          Tensor lat_lon = tilePixel.lat_lon().maps(suo);
          // establish longest string, e.g. "47.15"
          OptionalInt optionalInt = list.stream().map(Ticks::format).mapToInt(fontMetrics::stringWidth).max();
          if (gridlines) {
            graphics.setColor(gridLineCol);
            for (Scalar tick : list) {
              lat_lon.set(tick, 0);
              Point point = geoLayer.toPoint(lat_lon);
              graphics.drawLine(0, point.y, dimension.width, point.y);
            }
          }
          if (optionalInt.isPresent()) {
            int width = optionalInt.getAsInt() + 5;
            for (Scalar tick : list) {
              lat_lon.set(tick, 0);
              Point point = geoLayer.toPoint(lat_lon);
              int x = dimension.width - width;
              int y = point.y;
              graphics.setStroke(new BasicStroke()); // thickness of outline
              graphics.setColor(Color.BLACK);
              RenderQuality.smoothLine(graphics, false);
              graphics.drawLine(x, y, dimension.width, y);
              String string = Ticks.format(tick);
              textContour.draw(string, dimension.width - width, y - 2);
            }
          }
        }
        { // lon
          Scalar min = suo.apply(tilePixel.shift(-center.x, 0).lat_lon().Get(1));
          Scalar max = suo.apply(tilePixel.shift(+center.x, 0).lat_lon().Get(1));
          while (Scalars.lessThan(max, min))
            max = max.add(Quantity.of(360, "deg"));
          Clip clip = Clips.interval(min, max);
          if (Scalars.nonZero(clip.length())) {
            List<Scalar> list = Ticks.stream(clip, Rational.of(100, dimension.width)).toList();
            Tensor lat_lon = tilePixel.lat_lon().maps(suo);
            int height = fontMetrics.getDescent();
            for (Scalar tick : list) {
              lat_lon.set(tick, 1);
              Point point = geoLayer.toPoint(lat_lon);
              int x = point.x;
              if (gridlines) {
                graphics.setColor(gridLineCol);
                graphics.drawLine(x, 0, x, dimension.height);
              }
              int y = dimension.height - 20;
              graphics.setStroke(new BasicStroke()); // thickness of outline
              graphics.setColor(Color.BLACK);
              graphics.drawLine(x, y - 10, x, dimension.height);
              textContour.draw(" " + Ticks.format(tick), x, dimension.height - height - 2);
            }
          }
        }
      }
      if (availability) {
        MapImagesCache mapImagesCache = tileServer.cache();
        // IO.println("ZERO " + geoLayer.origin());
        Optional<TilePixel> optional = geoLayer.origin().zoomIncr(1, tileServer.z_max());
        if (optional.isPresent()) {
          // IO.println("ZOOM " + zoom);
          TilePixel zoom = optional.orElseThrow();
          graphics.setColor(new Color(255, 0, 0, 16));
          for (int ix = 0; ix < dimension.width * 2 + 256; ix += 256)
            for (int iy = 0; iy < dimension.height * 2 + 256; iy += 256) {
              TilePixel shift = zoom.shift(ix, iy);
              if (!mapImagesCache.isAvailableOffline(shift.tile())) {
                graphics.fillRect((ix - shift.pix()) / 2, (iy - shift.piy()) / 2, 128, 128);
              }
              // BufferedImage bufferedImage = mapImagesCache.getTile(shift.tile());
              // graphics.drawImage(bufferedImage, (ix - shift.pix()) / 2, (iy - shift.piy()) / 2, 128, 128, null);
            }
        }
      }
      if (showCycles) {
        graphics.setColor(marker);
        graphics.setStroke(new BasicStroke(4f));
        for (Tensor seg : segments) {
          Point p1 = geoLayer.toPoint(seg.get(0));
          Point p2 = geoLayer.toPoint(seg.get(1));
          graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
      }
      if (showPoi) {
        for (Pois pois : Pois.values()) {
          Point point = geoLayer.toPoint(pois.poi.vector());
          graphics.setColor(marker);
          graphics.setStroke(new BasicStroke(2));
          graphics.draw(new Ellipse2D.Double(point.x - 5, point.y - 5, 10, 10));
          textContour.draw(pois.name(), point.x + 8, point.y + 5);
        }
      }
      if (showGlobe && 4 < tilePixel.tile().z()) {
        ManifoldDisplay manifoldDisplay = S2Display.INSTANCE;
        Tensor pvm = PvmBuilder.rot().setOffset(100, 100).setPerPixel(100).digest();
        GeometricLayer geometricLayer = new GeometricLayer(pvm);
        manifoldDisplay.background().render(geometricLayer, graphics);
        {
          TilePixel p00 = tilePixel.shift(-center.x, -center.y);
          TilePixel p10 = tilePixel.shift(+center.x, -center.y);
          TilePixel p11 = tilePixel.shift(+center.x, +center.y);
          TilePixel p01 = tilePixel.shift(-center.x, +center.y);
          List<TilePixel> list = List.of(p00, p10, p11, p01);
          Tensor tensor = Tensor.of(list.stream().map(TilePixel::lat_lon).map(GeoPosition::of));
          new PathRender(ColorStroke.CURVE, manifoldDisplay.point2xy().slash(tensor), true) //
              .render(geometricLayer, graphics);
          manifoldDisplay.showPoints(ColorPairs.CONTROL_POINTS, RealScalar.ONE, tensor) //
              .render(geometricLayer, graphics);
        }
        final Scalar d_lat;
        {
          TilePixel pp0 = tilePixel.shift(-center.x, 0);
          TilePixel pm0 = tilePixel.shift(+center.x, 0);
          Tensor tensor = Tensor.of(Stream.of(pp0, pm0) //
              .map(TilePixel::lat_lon).map(GeoPosition::xyz));
          d_lat = Vector2Norm.between(tensor.get(0), tensor.get(1));
        }
        final Scalar d_lon;
        {
          TilePixel pp0 = tilePixel.shift(0, -center.y);
          TilePixel pm0 = tilePixel.shift(0, +center.y);
          Tensor tensor = Tensor.of(Stream.of(pp0, pm0) //
              .map(TilePixel::lat_lon).map(GeoPosition::xyz));
          d_lon = Vector2Norm.between(tensor.get(0), tensor.get(1));
        }
        textContour.draw("" + Tensors.of(d_lat, d_lon).maps(Round._2), 0, dimension.height - 40);
      }
      if (4 < tilePixel.tile().z() && minizoom != 0) {
        GeoComponent submap = new GeoComponent();
        Optional<TilePixel> optional = tilePixel.zoomIncr(-minizoom, tileServer.z_max());
        if (optional.isPresent()) {
          submap.tilePixel = optional.orElseThrow();
          submap.tileServer = geoComponent.tileServer;
          submap.setSize(new Dimension(minilen, minilen));
          graphics.setColor(Color.RED);
          Graphics2D gfx = (Graphics2D) graphics.create(dimension.width - minilen, 0, minilen, minilen);
          Shape shape = new Ellipse2D.Double(0, 0, minilen, minilen);
          gfx.setClip(shape);
          submap.printAll(gfx);
          gfx.setColor(Color.RED);
          gfx.setStroke(new BasicStroke(3f));
          gfx.draw(shape);
          {
            gfx.setColor(Color.BLACK);
            gfx.setStroke(new BasicStroke(1));
            Optional<TilePixel> zoomIncr = geoLayer.origin().zoomIncr(-minizoom, tileServer.z_max());
            if (zoomIncr.isPresent()) {
              TilePixel zoom = zoomIncr.orElseThrow();
              Point point = new GeoLayer(zoom).toPoint(submap.tilePixel);
              gfx.drawRect(minilen / 2 - point.x, minilen / 2 - point.y, 2 * point.x, 2 * point.y);
            }
          }
          gfx.dispose();
        }
      }
      if (8 <= tilePixel.tile().z()) {
        graphics.setStroke(new BasicStroke(2f));
        Path2D path2d = geoLayer.toPath2D(geoPath.pathSeq);
        graphics.setColor(Color.RED);
        graphics.draw(path2d);
        textContour.draw("" + geoPath.distance().maps(Round.FUNCTION), 0, 50);
      }
    };
  };
  private final Tensor segments = StaticHelper.segments();
  private GeoPath geoPath = new GeoPath();

  public MapViewer() {
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of( //
        Clips.interval(Quantity.of(37, "deg"), Quantity.of(44, "deg")), //
        Clips.interval(Quantity.of(-9, "deg"), Quantity.of(0, "deg")));
    // Quantity.of(38.343373, "deg"), Quantity.of(-0.762800, "deg") // Aspe
    BoxRandomSample boxRandomSample = new BoxRandomSample(cbb);
    geoComponent.tilePixel = TilePixel.from(8, RandomSample.of(boxRandomSample));
    geoComponent.tilePixel = TilePixel.from(9, Quantity.of(52.4, "deg"), Quantity.of(11.5, "deg"));
    LazyMouseListener lml = new LazyMouseListener() {
      @Override
      public void lazyClicked(MouseEvent mouseEvent) {
        TilePixel tilePixel = geoComponent.fromPoint(mouseEvent.getPoint());
        geoPath.pathSeq.append(tilePixel.lat_lon());
        geoComponent.repaint();
      }
    };
    new LazyMouse(lml).addListenersTo(geoComponent);
  }

  @Override
  public Container getContainer() {
    geoComponent.tileServer = tileServers;
    geoComponent.getCache().debug_print = debug_print;
    if (pathClear) {
      if (Tensors.nonEmpty(geoPath.pathSeq))
        geoPath.pathSeq = Drop.tail(geoPath.pathSeq, 1);
    }
    if (stats) {
      Dimension dimension = geoComponent.getSize();
      Point center = AwtUtil.center(dimension);
      // IO.println(geoComponent.tilePixel);
      TilePixel beg = geoComponent.tilePixel.shift(-center.x, -center.y);
      TilePixel end = geoComponent.tilePixel.shift(+center.x, +center.y);
      int z_start = beg.tile().z();
      // IO.println("START " + beg);
      for (int z = z_start; z <= Math.min(depth, geoComponent.tileServer.z_max()); ++z) {
        int count = 0;
        int total = 0;
        for (int x = beg.tile().x(); x <= end.tile().x(); ++x) {
          for (int y = beg.tile().y(); y <= end.tile().y(); ++y) {
            Tile tile = new Tile(beg.tile().z(), x, y);
            MapImagesCache mapImagesCache = geoComponent.getCache();
            if (mapImagesCache.isAvailableOffline(tile)) {
              ++count;
            } else
              mapImagesCache.getTile(tile);
            ++total;
          }
        }
        IO.println(z + " " + count + " / " + total);
        beg = beg.zoomIncr(1, geoComponent.tileServer.z_max()).orElseThrow();
        end = end.zoomIncr(1, geoComponent.tileServer.z_max()).orElseThrow();
      }
      stats = false;
    }
    return geoComponent;
  }

  static void main() {
    ResourceLocator resourceLocator = ResourceLocator.of(MapViewer.class);
    if (!FileBlock.of(resourceLocator.resolve(""))) {
      MapViewer mapViewer = new MapViewer();
      Path path = resourceLocator.resolve("leipzig2.properties");
      ObjectProperties.tryLoad(mapViewer.geoPath, path);
      JFrame jFrame = mapViewer.runStandalone();
      WindowClosed.runs(jFrame, () -> ObjectProperties.trySave(mapViewer.geoPath, path));
    }
  }
}
