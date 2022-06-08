// code by jph
package ch.alpine.ascona.avg;

import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.sophus.flt.ga.GeodesicExtrapolation;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.sca.win.WindowFunctions;

@ReflectionMarker
public class GeodesicSplitsDemo extends AbstractSplitsDemo {
  public WindowFunctions kernel = WindowFunctions.DIRICHLET;
  public Boolean prediction = false;

  public GeodesicSplitsDemo() {
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {2, 2, 1}, {5, 0, 2}}"));
  }

  @Override
  SymScalar symScalar(Tensor vector) {
    if (prediction)
      return 0 < vector.length() //
          ? (SymScalar) GeodesicExtrapolation.of(SymGeodesic.INSTANCE, kernel.get()).apply(vector)
          : null;
    if (!Integers.isEven(vector.length()))
      return (SymScalar) GeodesicCenter.of(SymGeodesic.INSTANCE, kernel.get()).apply(vector);
    return null;
  }

  public static void main(String[] args) {
    LookAndFeels.DEFAULT.updateUI();
    new GeodesicSplitsDemo().setVisible(1000, 600);
  }
}
