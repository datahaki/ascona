// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilders;
import ch.alpine.sophis.crv.clt.LagrangeQuadraticD;
import ch.alpine.sophis.crv.d2.ex.Arrowhead;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.sophus.lie.so2.ArcTan2D;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
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
  private static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 100);
  // private static final Tensor ARROWS = Subdivide.of(0.0, 1.0, 10);
  private static final ColorDataIndexed COLOR_DATA_INDEXED = //
      ColorDataLists._097.cyclic().deriveWithAlpha(192);
  // private static final PointsRender POINTS_RENDER = //
  // new PointsRender(new Color(0, 0, 0, 0), new Color(128, 128, 128, 64));

  public ClothoidDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2C_ONLY));
    setControlPointsSe2(Tensors.fromString("{{0,0,0},{3,0,0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor controlPointsSe2 = getControlPointsSe2();
    Tensor start = controlPointsSe2.get(0);
    Tensor mouse = controlPointsSe2.get(1);
    // ---
    {
      graphics.setColor(new Color(255, 0, 0, 128));
      geometricLayer.pushMatrix(Se2Matrix.of(mouse));
      graphics.fill(geometricLayer.toPath2D(Arrowhead.of(0.3)));
      geometricLayer.popMatrix();
    }
    int index = 0;
    for (ClothoidBuilders clothoidBuilders : ClothoidBuilders.values()) {
      Clothoid clothoid = clothoidBuilders.clothoidBuilder().curve(start, mouse);
      Tensor points = DOMAIN.map(clothoid);
      Color color = COLOR_DATA_INDEXED.getColor(index);
      new PathRender(color, 1.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
      // POINTS_RENDER.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.3), ARROWS.map(clothoid)) //
      // .render(geometricLayer, graphics);
      ++index;
      graphics.setColor(color);
      {
        Scalar angle = ArcTan2D.of(clothoid.apply(RealScalar.of(1e-8)));
        graphics.drawString(angle.map(Round._5) + "  " + clothoid, 0, index * 20);
        graphics.draw(geometricLayer.toLine2D(AngleVector.of(angle)));
      }
      {
        LagrangeQuadraticD lagrangeQuadraticD = clothoid.curvature();
        Scalar angle = lagrangeQuadraticD.head();
        graphics.draw(geometricLayer.toLine2D(AngleVector.of(angle).multiply(RealScalar.of(2))));
      }
    }
  }

  static void main() {
    launch();
  }
}
