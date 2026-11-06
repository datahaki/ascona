// code by jph
package ch.alpine.ascona.geo;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.RnCurveDecimation;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;

@ReflectionMarker
class GeoContoursDemo implements ManipulateProvider {
  public final BoundariesParam boundariesParam = new BoundariesParam();

  @Override
  public Container getContainer() {
    Tensor tensor = GeoBoundaries.INSTANCE.cache_apply(boundariesParam.country);
    if (Objects.isNull(tensor))
      return new JLabel("country unknown", SwingConstants.CENTER);
    Scalar epsilon = boundariesParam.epsilon();
    IO.println(epsilon);
    CurveDecimation curveDecimation = RnCurveDecimation.of(epsilon);
    // ---
    List<Show> list = new ArrayList<>();
    for (Tensor path : tensor) {
      Tensor deci = curveDecimation.apply(path);
      Show show = new Show();
      {
        show.add(PolygonPlot.of(path));
        show.add(PolygonPlot.of(deci));
      }
      show.setShowLabel(path.length() + " " + deci.length());
      show.setAspectRatioOne();
      list.add(show);
    }
    return ShowGridComponent.of(list);
  }

  static void main() {
    new GeoContoursDemo().runStandalone();
  }
}
