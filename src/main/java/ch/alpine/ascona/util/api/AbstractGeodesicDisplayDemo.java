// code by jph
package ch.alpine.ascona.util.api;

import java.awt.Dimension;
import java.util.List;

import ch.alpine.ascona.util.dis.GeodesicDisplayRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;

@ReflectionMarker
public abstract class AbstractGeodesicDisplayDemo extends AbstractDemo {
  private final SpinnerLabel<ManifoldDisplay> manifoldDisplaySpinner = new SpinnerLabel<>();
  private final List<ManifoldDisplay> list;

  public AbstractGeodesicDisplayDemo(List<ManifoldDisplay> list) {
    if (list.isEmpty())
      throw new RuntimeException();
    this.list = list;
    manifoldDisplaySpinner.setList(list);
    manifoldDisplaySpinner.setValue(list.get(0));
    if (1 < list.size()) {
      manifoldDisplaySpinner.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "geodesic type");
      timerFrame.jToolBar.addSeparator();
    }
    timerFrame.geometricComponent.addRenderInterfaceBackground(new GeodesicDisplayRender() {
      @Override
      public ManifoldDisplay getGeodesicDisplay() {
        return manifoldDisplay();
      }
    });
  }

  /** @return */
  public final ManifoldDisplay manifoldDisplay() {
    return manifoldDisplaySpinner.getValue();
  }

  public synchronized final void setManifoldDisplay(ManifoldDisplay manifoldDisplay) {
    manifoldDisplaySpinner.setValue(manifoldDisplay);
  }

  public void addSpinnerListener(SpinnerListener<ManifoldDisplay> spinnerListener) {
    manifoldDisplaySpinner.addSpinnerListener(spinnerListener);
  }

  /** @return */
  public List<ManifoldDisplay> getManifoldDisplays() {
    return list;
  }
}
