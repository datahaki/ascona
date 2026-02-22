// code by jph
package ch.alpine.euclid;

import java.awt.Graphics2D;

import ch.alpine.ascona.crv.AbstractCurvatureDemo;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.GeodesicBSplineFunction;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.itp.BSplineFunction;
import ch.alpine.tensor.itp.BSplineFunctionCyclic;
import ch.alpine.tensor.itp.BSplineFunctionString;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.Clips;

/** use of tensor lib {@link BSplineFunction}
 * 
 * {@link GeodesicBSplineFunction} */
public class BSplineFunctionDemo extends AbstractCurvatureDemo {
  @ReflectionMarker
  public static class Param extends AbstractCurvatureParam {
    public Param() {
      super(ManifoldDisplays.R2_ONLY);
    }

    @FieldClip(min = "0", max = "9")
    public Integer degree = 3;
    @FieldClip(min = "1", max = "1000")
    public Integer points = 100;
    public Boolean cyclic = false;
  }

  private final Param param;

  public BSplineFunctionDemo() {
    this(new Param());
  }

  public BSplineFunctionDemo(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    timerFrame.geometricComponent.renderGrid(graphics);
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    Tensor refined = Tensors.empty();
    int n = control.length();
    if (0 < n) {
      int _degree = param.degree;
      if (param.cyclic) {
        refined = Subdivide.intermediate_increasing(Clips.interval(0.0, n), param.points) //
            .maps(BSplineFunctionCyclic.of(_degree, control));
      } else {
        refined = Subdivide.of(0, n - 1, param.points) //
            .maps(BSplineFunctionString.of(_degree, control));
      }
    } else {
      refined = CirclePoints.of(7);
    }
    Curvature2DRender.of(refined, param.cyclic).render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    return refined;
  }

  static void main() {
    new BSplineFunctionDemo().runStandalone();
  }
}
