// code by jph
package ch.alpine.ascona.euclid;

import java.util.List;

import javax.swing.JComponent;

import ch.alpine.ascony.api.RnLineTrim;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.pow.Sqrt;

@ReflectionMarker
class CircleQualityDemo implements ManipulateProvider {
  @FieldSlider
  @FieldClip(min = "1", max = "40")
  public Scalar quality = RealScalar.of(10);

  @Override
  public JComponent getContainer() {
    Show show1 = new Show();
    Show show2 = new Show();
    for (Tensor _x : Subdivide.of(0.1, 2, 20)) {
      Scalar radius = (Scalar) _x;
      int n = Math.max(3, Ceiling.intValueExact(Sqrt.FUNCTION.apply(radius).multiply(quality)));
      Tensor curve = CirclePoints.of(n).multiply(radius);
      curve.append(curve.get(0));
      show1.add(ListLinePlot.of(Subdivide.increasing(Clips.unit(), curve.length() - 1), //
          RnLineTrim.TRIPLE_REDUCE_EXTRAPOLATION.apply(curve)));
      show1.setPlotLabel("quality = " + N.DOUBLE.apply(quality));
      show2.add(ListLinePlot.of(curve));
    }
    show2.setAspectRatioOne();
    return ShowGridComponent.of(List.of(show1, show2));
  }

  static void main() {
    new CircleQualityDemo().runStandalone();
  }
}
