// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidComparators;
import ch.alpine.sophis.crv.clt.ClothoidContext;
import ch.alpine.sophis.crv.clt.ClothoidEmit;
import ch.alpine.sophis.crv.clt.ClothoidSolutions;
import ch.alpine.sophis.crv.clt.ClothoidTangentDefect;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.Clips;

/** The demo shows that when using LaneRiesenfeldCurveSubdivision(Clothoid.INSTANCE, degree)
 * in order to connect two points p and q, then the (odd) degree has little influence on the
 * resulting curve. The difference is only noticeable for S shaped curves.
 * 
 * Therefore, for simplicity in algorithms we use degree == 1. */
public class ClothoidEmitDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = //
      ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final Scalar minResolution = RealScalar.of(0.02);

  public ClothoidEmitDemo() {
    super(new AsconaParam(false));
    setControlPointsSe2(Tensors.fromString("{{0,0,0}, {3,0,0}}"));
  }

  @Override
  public List<ManifoldDisplays> getManifoldDisplays() {
    return ManifoldDisplays.SE2C_ONLY;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    Tensor start = control.get(0);
    Tensor mouse = control.get(1);
    // ---
    List<Clothoid> list = List.of();
    ClothoidContext clothoidContext = new ClothoidContext(start, mouse);
    ClothoidTangentDefect clothoidTangentDefect = ClothoidTangentDefect.of(clothoidContext);
    ClothoidSolutions clothoidSolutions = new ClothoidSolutions(clothoidTangentDefect, Clips.absolute(20));
    list = ClothoidEmit.stream(clothoidContext, clothoidSolutions.lambdas()).toList();
    int index = 0;
    for (Clothoid clothoid : list) {
      ClothoidTransition clothoidTransition = ClothoidTransition.of(start, mouse, clothoid);
      Tensor points = clothoidTransition.linearized(minResolution);
      new PathRender(COLOR_DATA_INDEXED.getColor(index++), 1.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
    }
    // ---
    Optional<Clothoid> optional = list.stream().min(ClothoidComparators.CURVATURE_HEAD);
    if (optional.isPresent()) {
      Clothoid clothoid = optional.orElseThrow();
      ClothoidTransition clothoidTransition = ClothoidTransition.of(start, mouse, clothoid);
      Tensor points = clothoidTransition.linearized(minResolution);
      new PathRender(Color.BLACK, 2.5f) //
          .setCurve(points, false).render(geometricLayer, graphics);
    }
  }

  static void main() {
    new ClothoidEmitDemo().runStandalone();
  }
}
