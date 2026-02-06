// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.BSplineFunction;
import ch.alpine.tensor.itp.BSplineFunctionCyclic;
import ch.alpine.tensor.itp.BSplineFunctionString;

/** use of tensor lib {@link BSplineFunction} */
public class R2BSplineFunctionDemo extends AbstractCurvatureDemo {
  @ReflectionMarker
  public static class Param extends AbstractCurvatureParam {
    public Param() {
      super(ManifoldDisplays.R2_ONLY);
    }

    @FieldClip(min = "0", max = "9")
    public Integer degree = 3;
    public Boolean cyclic = false;
  }

  private final Param param;

  public R2BSplineFunctionDemo() {
    this(new Param());
  }

  public R2BSplineFunctionDemo(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    Tensor refined = Tensors.empty();
    if (0 < control.length()) {
      int _degree = param.degree;
      ScalarTensorFunction scalarTensorFunction = param.cyclic //
          ? BSplineFunctionCyclic.of(_degree, control)
          : BSplineFunctionString.of(_degree, control);
      refined = Subdivide.of(0, param.cyclic ? control.length() : control.length() - 1, 100) //
          .map(scalarTensorFunction);
      new PathRender(Color.BLUE).setCurve(refined, param.cyclic).render(geometricLayer, graphics);
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    return refined;
  }

  static void main() {
    launch();
  }
}
