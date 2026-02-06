// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.Se2CoveringDisplay;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidBuilders;
import ch.alpine.sophis.crv.dub.DubinsPath;
import ch.alpine.sophis.crv.dub.DubinsPathComparators;
import ch.alpine.sophis.crv.dub.DubinsPathGenerator;
import ch.alpine.sophis.crv.dub.DubinsRadius;
import ch.alpine.sophis.crv.dub.DubinsType;
import ch.alpine.sophis.crv.dub.FixedRadiusDubins;
import ch.alpine.sophis.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.PadLeft;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.chq.FiniteScalarQ;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.sca.Clips;

public class DubinsPathDemo extends ControlPointsDemo {
  private static final ClothoidBuilder CLOTHOID_BUILDER = ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder();
  private static final int POINTS = 200;
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.SE2_ONLY);
    }

    public Boolean allDubins = false;
    public Boolean relax = true;
    public Boolean shortest = true;
    public Boolean clothoid = true;
  }

  private final Param param;
  private final PathRender pathRender = new PathRender(Color.RED, 2f);
  private final PathRender pathRenderClothoid = new PathRender(Color.CYAN, 2f);

  public DubinsPathDemo() {
    this(new Param());
  }

  public DubinsPathDemo(Param param) {
    super(param);
    this.param = param;
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {3, 0, 0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor controlPointsSe2 = getControlPointsSe2();
    Tensor START = controlPointsSe2.get(0);
    Tensor mouse = controlPointsSe2.get(1);
    // ---
    DubinsPathGenerator dubinsPathGenerator = FixedRadiusDubins.of(START, mouse, RealScalar.of(1));
    List<DubinsPath> list = dubinsPathGenerator.stream().toList();
    if (param.allDubins) {
      graphics.setColor(COLOR_DATA_INDEXED.getColor(0));
      graphics.setStroke(new BasicStroke(1f));
      if (param.relax) { // draw shortest path
        for (DubinsType dubinsType : DubinsType.values()) {
          Scalar maxRadius = DubinsRadius.getMax(mouse, dubinsType, Clips.interval(0.5, 2));
          if (FiniteScalarQ.of(maxRadius)) {
            Optional<DubinsPath> optional = FixedRadiusDubins.of(mouse, dubinsType, maxRadius);
            if (optional.isPresent()) {
              graphics.draw(geometricLayer.toPath2D(sample(optional.get())));
            }
          }
        }
      } else
        for (DubinsPath dubinsPath : list)
          graphics.draw(geometricLayer.toPath2D(sample(dubinsPath)));
    }
    if (param.shortest) { // draw shortest path
      graphics.setColor(COLOR_DATA_INDEXED.getColor(1));
      graphics.setStroke(new BasicStroke(2f));
      DubinsPath dubinsPath = list.stream().min(DubinsPathComparators.LENGTH).orElseThrow();
      graphics.draw(geometricLayer.toPath2D(sample(dubinsPath)));
    }
    {
      DubinsPath dubinsPath = list.stream().min(DubinsPathComparators.LENGTH).orElseThrow();
      ScalarTensorFunction scalarTensorFunction = dubinsPath.sampler(START);
      Tensor params = PadLeft.zeros(4).apply(dubinsPath.segments());
      graphics.setColor(new Color(128, 128, 128, 128));
      // graphics.setColor(COLOR_DATA_INDEXED.getColor(3));
      Tensor map = params.map(scalarTensorFunction);
      {
        LeversRender leversRender = LeversRender.of(Se2CoveringDisplay.INSTANCE, map, null, geometricLayer, graphics);
        leversRender.renderSequence();
        leversRender.renderIndexP();
      }
      BSpline3CurveSubdivision bSpline3CurveSubdivision = //
          new BSpline3CurveSubdivision(Se2CoveringGroup.INSTANCE);
      Tensor points = Nest.of(bSpline3CurveSubdivision::string, map, 5);
      // graphics.setStroke(new BasicStroke(2f));
      pathRender.setCurve(points, false).render(geometricLayer, graphics);
    }
    if (param.clothoid) { // draw clothoid
      ClothoidTransition clothoidTransition = //
          ClothoidTransition.of(CLOTHOID_BUILDER, START, mouse);
      Tensor tensor = clothoidTransition.linearized(RealScalar.of(0.1));
      pathRenderClothoid.setCurve(tensor, false).render(geometricLayer, graphics);
      // TODO ASCONA ALG
    }
    { // draw least curved path
      graphics.setColor(COLOR_DATA_INDEXED.getColor(2));
      graphics.setStroke(new BasicStroke(2f));
      DubinsPath dubinsPath = list.stream().min(DubinsPathComparators.TOTAL_CURVATURE).get();
      graphics.draw(geometricLayer.toPath2D(sample(dubinsPath)));
    }
  }

  private Tensor sample(DubinsPath dubinsPath) {
    Tensor controlPointsSe2 = getControlPointsSe2();
    Tensor START = controlPointsSe2.get(0);
    return Subdivide.of(0.0, 1.0, POINTS).map(dubinsPath.unit(START));
  }

  static void main() {
    launch();
  }
}
