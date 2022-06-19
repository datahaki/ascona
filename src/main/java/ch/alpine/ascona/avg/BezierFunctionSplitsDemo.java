// code by jph
package ch.alpine.ascona.avg;

import java.awt.Dimension;

import javax.swing.JSlider;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.crv.BezierFunction;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarTensorFunction;

/** visualization of geodesic average along geodesics */
@ReflectionMarker
public class BezierFunctionSplitsDemo extends AbstractSplitsDemo {
  private final JSlider jSlider = new JSlider(0, 1000, 500);

  public BezierFunctionSplitsDemo() {
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    jSlider.setPreferredSize(new Dimension(500, 28));
    timerFrame.jToolBar.add(jSlider);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {2, 2, 1}, {5, 0, 2}}"));
    // ---
    setManifoldDisplay(ManifoldDisplays.S2);
    setControlPointsSe2(Tensors.fromString("{}"));
  }

  @Override // from GeodesicAverageDemo
  SymScalar symScalar(Tensor vector) {
    int n = vector.length();
    if (0 < n) {
      ScalarTensorFunction scalarTensorFunction = new BezierFunction(SymGeodesic.INSTANCE, vector);
      Scalar parameter = n <= 1 //
          ? RealScalar.ZERO
          : RationalScalar.of(n, n - 1);
      parameter = parameter.multiply(RationalScalar.of(jSlider.getValue(), 1000));
      return (SymScalar) scalarTensorFunction.apply(parameter);
    }
    return null;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new BezierFunctionSplitsDemo().setVisible(1000, 600);
  }
}
