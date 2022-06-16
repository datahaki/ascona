// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.alpine.ascona.util.dis.Se2CoveringDisplay;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidBuilders;
import ch.alpine.sophus.crv.clt.ClothoidTransition;
import ch.alpine.sophus.crv.dub.DubinsPath;
import ch.alpine.sophus.crv.dub.DubinsPathComparators;
import ch.alpine.sophus.crv.dub.DubinsPathGenerator;
import ch.alpine.sophus.crv.dub.DubinsRadius;
import ch.alpine.sophus.crv.dub.DubinsType;
import ch.alpine.sophus.crv.dub.FixedRadiusDubins;
import ch.alpine.sophus.lie.se2c.Se2CoveringGroup;
import ch.alpine.sophus.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.PadLeft;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.chq.FiniteScalarQ;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.sca.Clips;

public class DubinsPathDemo extends AbstractDemo {
  private static final ClothoidBuilder CLOTHOID_BUILDER = ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder();
  private static final Tensor START = Array.zeros(3).unmodifiable();
  private static final int POINTS = 200;
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();

  @ReflectionMarker
  public static class Param {
    public Boolean allDubins = true;
    public Boolean relax = true;
    public Boolean shortest = true;
    public Boolean clothoid = true;
  }

  private final Param param = new Param();
  private final PathRender pathRender = new PathRender(Color.RED, 2f);
  private final PathRender pathRenderClothoid = new PathRender(Color.CYAN, 2f);

  public DubinsPathDemo() {
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    // ---
    DubinsPathGenerator dubinsPathGenerator = FixedRadiusDubins.of(START, mouse, RealScalar.of(1));
    List<DubinsPath> list = dubinsPathGenerator.stream().collect(Collectors.toList());
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
      // TODO OWL ALG
    }
    { // draw least curved path
      graphics.setColor(COLOR_DATA_INDEXED.getColor(2));
      graphics.setStroke(new BasicStroke(2f));
      DubinsPath dubinsPath = list.stream().min(DubinsPathComparators.TOTAL_CURVATURE).get();
      graphics.draw(geometricLayer.toPath2D(sample(dubinsPath)));
    }
  }

  private static Tensor sample(DubinsPath dubinsPath) {
    return Subdivide.of(0.0, 1.0, POINTS).map(dubinsPath.unit(START));
  }

  public static void main(String[] args) {
    LookAndFeels.INTELLI_J.tryUpdateUI();
    new DubinsPathDemo().setVisible(1000, 600);
  }
}
