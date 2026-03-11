// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import ch.alpine.ascony.api.Spearhead;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.fig.PlotOption;
import ch.alpine.bridge.fig.PolygonPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.PolygonArea;
import ch.alpine.sophis.crv.d2.PolygonCentroid;
import ch.alpine.sophis.crv.d2.PolygonNormalize;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.Round;

class SpearheadDemo extends ClothoidBaseDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(128);

  @ReflectionMarker
  static class Param {
    @FieldSlider
    @FieldClip(min = "0.1", max = "30")
    public Scalar pix2mod = RealScalar.of(10);
  }

  private final Param param;

  public SpearheadDemo() {
    super(param = new Param());
    setControlPointsSe2(Tensors.fromString("{{-0.5, -0.5, 0.3}}"));
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    Scalar res = geometricLayer.pixel2modelFactor(param.pix2mod);
    Tensor polygon = Spearhead.of(control.get(0), res);
    graphics.setColor(COLOR_DATA_INDEXED.getColor(1));
    graphics.fill(geometricLayer.toPath2D(polygon));
    new PathRender(COLOR_DATA_INDEXED.getColor(0), 1.5f) //
        .setCurve(polygon, false) //
        .render(geometricLayer, graphics);
    Scalar area = PolygonArea.of(polygon);
    Tensor centroid = PolygonCentroid.of(polygon);
    graphics.setColor(Color.DARK_GRAY);
    graphics.drawString("" + area.maps(Round._5), 100, 100);
    graphics.drawString("" + control.get(0).maps(Round._5), 100, 120);
    graphics.drawString("" + res.maps(Round._5), 100, 140);
    Point2D point2d = geometricLayer.toPoint2D(centroid);
    graphics.drawRect((int) point2d.getX() - 2, (int) point2d.getY() - 2, 4, 4);
    Tensor tensor = PolygonNormalize.of(polygon, RealScalar.ONE);
    try {
      Show show = new Show();
      show.add(PolygonPlot.of(tensor, PlotOption.FILL)).setAlpha(64);
      show.render_autoIndent(graphics, new Rectangle(0, 0, 300, 300));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void main() {
    new SpearheadDemo().runStandalone();
  }
}
