// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2ClothoidDisplay;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidBuilders;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.sophus.ref.d1.LaneRiesenfeldCurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.red.Nest;

/** The demo shows that when using LaneRiesenfeldCurveSubdivision(Clothoid.INSTANCE, degree)
 * in order to connect two points p and q, then the (odd) degree has little influence on the
 * resulting curve. The difference is only noticeable for S shaped curves.
 * 
 * Therefore, for simplicity in algorithms we use degree == 1. */
public class ClothoidLRDemo extends ControlPointsDemo {
  private static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 100);
  private static final Tensor ARROWS = Subdivide.of(0.0, 1.0, 8);
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final PointsRender POINTS_RENDER_C = new PointsRender(new Color(0, 0, 0, 0), new Color(128, 128, 128, 64));
  private static final PointsRender POINTS_RENDER_S = new PointsRender(new Color(0, 0, 0), Color.BLACK);

  public ClothoidLRDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2C_ONLY));
    setControlPointsSe2(Tensors.fromString("{{0,0,0}, {3,0,0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor control = getGeodesicControlPoints();
    Tensor start = control.get(0);
    Tensor mouse = control.get(1);
    // ---
    {
      graphics.setColor(new Color(255, 0, 0, 128));
      geometricLayer.pushMatrix(GfxMatrix.of(mouse));
      graphics.fill(geometricLayer.toPath2D(Arrowhead.of(0.3)));
      geometricLayer.popMatrix();
    }
    int index = 0;
    for (ClothoidBuilder clothoidBuilder : new ClothoidBuilder[] { //
        ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder(), ClothoidBuilders.SE2_LEGENDRE.clothoidBuilder() }) {
      Clothoid clothoid = clothoidBuilder.curve(start, mouse);
      Tensor points = DOMAIN.map(clothoid);
      new PathRender(COLOR_DATA_INDEXED.getColor(index), 1.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
      POINTS_RENDER_C.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.3), ARROWS.map(clothoid)) //
          .render(geometricLayer, graphics);
      ++index;
    }
    {
      CurveSubdivision curveSubdivision = LaneRiesenfeldCurveSubdivision.of(ClothoidBuilders.SE2_LEGENDRE.clothoidBuilder(), 1);
      Tensor points = Nest.of(curveSubdivision::string, Tensors.of(start, mouse), 2); // length == 129
      new PathRender(COLOR_DATA_INDEXED.getColor(2), 2.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
      POINTS_RENDER_S.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.3), points) //
          .render(geometricLayer, graphics);
    }
    {
      CurveSubdivision curveSubdivision = LaneRiesenfeldCurveSubdivision.of(ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder(), 1);
      Tensor points = Nest.of(curveSubdivision::string, Tensors.of(start, mouse), 2); // length == 129
      new PathRender(COLOR_DATA_INDEXED.getColor(2), 2.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
      POINTS_RENDER_S.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.3), points) //
          .render(geometricLayer, graphics);
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
