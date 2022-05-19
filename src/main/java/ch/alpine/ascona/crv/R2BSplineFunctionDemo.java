// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.BSplineFunction;
import ch.alpine.tensor.itp.BSplineFunctionCyclic;
import ch.alpine.tensor.itp.BSplineFunctionString;

/** use of tensor lib {@link BSplineFunction} */
@ReflectionMarker
public class R2BSplineFunctionDemo extends AbstractCurvatureDemo {
  @FieldInteger
  @FieldSelectionArray(value = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
  public Scalar degree = RealScalar.of(3);
  public Boolean cyclic = false;

  public R2BSplineFunctionDemo() {
    super(ManifoldDisplays.R2_ONLY);
    // ---
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
  }

  @Override
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    Tensor refined = Tensors.empty();
    if (0 < control.length()) {
      int _degree = degree.number().intValue();
      ScalarTensorFunction scalarTensorFunction = cyclic //
          ? BSplineFunctionCyclic.of(_degree, control)
          : BSplineFunctionString.of(_degree, control);
      refined = Subdivide.of(0, cyclic ? control.length() : control.length() - 1, 100) //
          .map(scalarTensorFunction);
      new PathRender(Color.BLUE).setCurve(refined, cyclic).render(geometricLayer, graphics);
    }
    RenderQuality.setQuality(graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    return refined;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new R2BSplineFunctionDemo().setVisible(1200, 600);
  }
}
