// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilders;
import ch.alpine.sophis.crv.clt.LagrangeQuadraticD;
import ch.alpine.sophis.crv.d2.Curvature2D;
import ch.alpine.sophus.lie.so2.ArcTan2D;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.rot.AngleVector;
import ch.alpine.tensor.sca.Round;

/** The demo shows that when using LaneRiesenfeldCurveSubdivision(Clothoid.INSTANCE, degree)
 * in order to connect two points p and q, then the (odd) degree has little influence on the
 * resulting curve. The difference is only noticeable for S shaped curves.
 * 
 * Therefore, for simplicity in algorithms we use degree == 1. */
public class ClothoidDemo extends ControlPointsDemo {
  private static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 200);
  // private static final Tensor ARROWS = Subdivide.of(0.0, 1.0, 10);
  private static final ColorDataIndexed COLOR_DATA_INDEXED = //
      ColorDataLists._097.cyclic().deriveWithAlpha(192);
  // private static final PointsRender POINTS_RENDER = //
  // new PointsRender(new Color(0, 0, 0, 0), new Color(128, 128, 128, 64));

  public ClothoidDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2C_ONLY));
    setControlPointsSe2(Tensors.fromString("{{2,0,0},{-2,0,0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    Tensor start = control.get(0);
    Tensor mouse = control.get(1);
    // ---
    Show show = new Show();
    show.setPlotLabel("Curvature Comparison");
    int index = 0;
    TensorUnaryOperator tuo = v -> v.extract(0, 2);
    for (ClothoidBuilders clothoidBuilders : new ClothoidBuilders[] { //
        ClothoidBuilders.SE2_ANALYTIC, //
        ClothoidBuilders.SE2_LEGENDRE }) {
      Clothoid clothoid = clothoidBuilders.clothoidBuilder().curve(start, mouse);
      Tensor points = DOMAIN.maps(clothoid);
      Color color = COLOR_DATA_INDEXED.getColor(index);
      new PathRender(color, 1.0f).setCurve(points, false).render(geometricLayer, graphics);
      graphics.setColor(color);
      {
        Scalar angle = ArcTan2D.of(clothoid.apply(RealScalar.of(1e-8)));
        graphics.drawString(angle.maps(Round._5).toString(), 0, 40 + index * 20);
        graphics.draw(geometricLayer.toLine2D(AngleVector.of(angle)));
      }
      {
        LagrangeQuadraticD lagrangeQuadraticD = clothoid.curvature();
        Scalar angle = lagrangeQuadraticD.head();
        graphics.draw(geometricLayer.toLine2D(AngleVector.of(angle).multiply(RealScalar.of(2))));
      }
      Tensor string = Curvature2D.string(tuo.slash(points));
      Showable showable = show.add(ListLinePlot.of(DOMAIN, string));
      showable.setLabel(clothoid.toString());
      ++index;
    }
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    show.render_autoIndent(graphics, new Rectangle(dimension.width / 2, 0, dimension.width / 2, dimension.height));
  }

  static void main() {
    launch();
  }
}
