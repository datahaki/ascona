// code by jph
package ch.alpine.ascona.lev;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorScalarFunction;

public abstract class LogWeightingBase extends ControlPointsDemo {
  protected final SpinnerLabel<LogWeighting> spinnerLogWeighting;

  public LogWeightingBase(boolean addRemoveControlPoints, List<ManifoldDisplays> list, List<LogWeighting> array) {
    super(new AsconaParam(addRemoveControlPoints, list));
    {
      spinnerLogWeighting = SpinnerLabel.of(array);
      if (array.contains(LogWeightings.COORDINATE))
        spinnerLogWeighting.setValue(LogWeightings.COORDINATE);
      else
        spinnerLogWeighting.setValue(array.get(0));
      if (1 < array.size())
        spinnerLogWeighting.addToComponentReduced(timerFrame.jToolBar, new Dimension(150, 28), "weights");
    }
    timerFrame.jToolBar.addSeparator();
  }

  public final void addMouseRecomputation() {
    MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        switch (mouseEvent.getButton()) {
        case MouseEvent.BUTTON1: // insert point
          if (!controlPointsRender.isPositioningOngoing())
            recompute();
          break;
        default:
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        if (controlPointsRender.isPositioningOngoing())
          recompute();
      }
    };
    // ---
    timerFrame.geometricComponent.jComponent.addMouseListener(mouseAdapter);
    timerFrame.geometricComponent.jComponent.addMouseMotionListener(mouseAdapter);
  }

  /** Hint: override is possible for customization
   * 
   * @return */
  protected LogWeighting logWeighting() {
    return spinnerLogWeighting.getValue();
  }

  protected final void setLogWeighting(LogWeighting logWeighting) {
    spinnerLogWeighting.setValue(logWeighting);
    spinnerLogWeighting.reportToAll();
  }

  protected abstract Sedarim operator(Tensor sequence);

  protected abstract TensorScalarFunction function(Tensor sequence, Tensor values);

  protected abstract void recompute();
}
