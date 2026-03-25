// code by jph
package ch.alpine.ascona.euclid.hil;

import java.util.List;

import javax.swing.JComponent;

import ch.alpine.ascony.crv.HilbertPolygon;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.pow.Power;

@ReflectionMarker
public class HilbertPolygonDemo implements ManipulateProvider {
  private static final CoordinateBoundingBox CBB = CoordinateBoundingBox.of(Clips.unit(), Clips.unit());
  // ---
  @FieldSlider
  @FieldClip(min = "1", max = "7")
  public Integer n = 2;

  @Override
  public JComponent getContainer() {
    Show show = new Show();
    show.setCbb(CBB);
    show.add(ListLinePlot.of(HilbertPolygon.of(n).multiply(Power.of(2.0, -n))));
    return ShowGridComponent.of(List.of(show));
  }

  static void main() {
    new HilbertPolygonDemo().runStandalone();
  }
}
