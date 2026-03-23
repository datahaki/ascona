// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.sophis.crv.BezierCurve;
import ch.alpine.sophus.bm.LinearBiinvariantMean;
import ch.alpine.sophus.clt.Clothoid;
import ch.alpine.sophus.clt.ClothoidBuilders;
import ch.alpine.sophus.lie.so2.ArcTan2D;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;

class CubicBezierClothoidDemo extends EuclideanPlaneDemo {
  public CubicBezierClothoidDemo() {
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 400).setPerPixel(100).digest();
    geometricComponent().setModel2Pixel(pvm);
    setControlPointsSe2(Tensors.fromString("{{0,0,0},{1,0,0},{2,1,0},{3,1,0}}"));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
    leversRender.renderSequence();
    leversRender.renderIndexP();
    Tensor domain = Subdivide.of(0.0, 1.0, 20);
    {
      ScalarTensorFunction stf = BezierCurve.of(LinearBiinvariantMean.INSTANCE, sequence);
      Tensor curve = domain.maps(stf);
      new PathRender(ColorStroke.CURVE, curve, false).render(geometricLayer, graphics);
    }
    Tensor p0 = sequence.get(0);
    Tensor p1 = sequence.get(1);
    Tensor p2 = sequence.get(2);
    Tensor p3 = sequence.get(3);
    {
      Tensor p = sequence.get(0).append(ArcTan2D.of(p1.subtract(p0)));
      Tensor q = sequence.get(3).append(ArcTan2D.of(p3.subtract(p2)));
      Clothoid clothoid = ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder().curve(p, q);
      Tensor curve = domain.maps(clothoid);
      new PathRender(ColorStroke.SECONDARY_CURVE, curve, false).render(geometricLayer, graphics);
    }
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  static void main() {
    new CubicBezierClothoidDemo().runStandalone();
  }
}
