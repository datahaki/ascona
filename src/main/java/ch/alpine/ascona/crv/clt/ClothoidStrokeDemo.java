// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidBuilders;
import ch.alpine.sophus.crv.clt.LagrangeQuadraticD;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.lie.se2.Se2GroupElement;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.exp.Exp;

/** The demo shows that when using LaneRiesenfeldCurveSubdivision(Clothoid.INSTANCE, degree)
 * in order to connect two points p and q, then the (odd) degree has little influence on the
 * resulting curve. The difference is only noticeable for S shaped curves.
 * 
 * Therefore, for simplicity in algorithms we use degree == 1. */
public class ClothoidStrokeDemo extends ControlPointsDemo {
  private static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 100);
  private static final ColorDataIndexed COLOR_DATA_INDEXED = //
      ColorDataLists._097.cyclic().deriveWithAlpha(192);

  public ClothoidStrokeDemo() {
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
    ClothoidBuilder clothoidBuilder = ClothoidBuilders.SE2_COVERING.clothoidBuilder();
    {
      Clothoid clothoid = clothoidBuilder.curve(start, mouse);
      Tensor points = DOMAIN.map(clothoid);
      Color color = COLOR_DATA_INDEXED.getColor(0);
      new PathRender(color, 1.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
      LagrangeQuadraticD lagrangeQuadraticD = clothoid.curvature();
      Tensor above = Tensors.empty();
      Tensor below = Tensors.empty();
      for (Tensor _t : DOMAIN) {
        Scalar t = (Scalar) _t;
        Se2GroupElement se2GroupElement = new Se2GroupElement(clothoid.apply(t));
        Scalar curvature = lagrangeQuadraticD.apply(t);
        Scalar radius = Exp.FUNCTION.apply(curvature.multiply(curvature).negate());
        above.append(se2GroupElement.combine(Tensors.of(radius.zero(), radius, RealScalar.ZERO)));
        below.append(se2GroupElement.combine(Tensors.of(radius.zero(), radius.negate(), RealScalar.ZERO)));
      }
      new PathRender(color, 1.5f) //
          .setCurve(above, false).render(geometricLayer, graphics);
      new PathRender(color, 1.5f) //
          .setCurve(below, false).render(geometricLayer, graphics);
      Tensor tensor = Join.of(above, Reverse.of(below));
      graphics.fill(geometricLayer.toPath2D(tensor));
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new ClothoidStrokeDemo().setVisible(1000, 600);
  }
}
