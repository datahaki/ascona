// code by jph
package ch.alpine.ascona.lev;

import java.util.List;
import java.util.Optional;

import javax.swing.JButton;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.Round;

public abstract class AbstractPlaceDemo extends ControlPointsDemo {
  private final JButton jButtonPrint = new JButton("print");

  public AbstractPlaceDemo(boolean addRemoveControlPoints, List<ManifoldDisplays> list) {
    super(addRemoveControlPoints, list);
    setMidpointIndicated(false);
    // ---
    jButtonPrint.addActionListener(l -> System.out.println(getControlPointsSe2().map(Round._3)));
    timerFrame.jToolBar.add(jButtonPrint);
  }

  protected final Optional<Tensor> getOrigin() {
    Tensor geodesicControlPoints = getGeodesicControlPoints(0, 1);
    return 0 < geodesicControlPoints.length() //
        ? Optional.of(geodesicControlPoints.get(0))
        : Optional.empty();
  }

  protected final Tensor getSequence() {
    return getGeodesicControlPoints(1, Integer.MAX_VALUE);
  }
}
