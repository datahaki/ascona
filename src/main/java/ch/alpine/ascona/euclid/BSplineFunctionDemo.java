// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.ref.BaseCurvatureParam;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
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
class BSplineFunctionDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param extends BaseCurvatureParam {
    @FieldClip(min = "0", max = "9")
    public Integer degree = 3;
    @FieldClip(min = "1", max = "1000")
    public Integer points = 100;
    public Boolean cyclic = false;
  }

  private final Param param;

  public BSplineFunctionDemo() {
    super(param = new Param());
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointTypes.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    new GridRender(timerFrame.geometricComponent.jComponent::getSize).render(geometricLayer, graphics);
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
    param.spawn(manifoldDisplay(), refined, new Rectangle(0, 0, 400, 300)) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    new BSplineFunctionDemo().runStandalone();
  }
}
