// code by jph
package ch.alpine.ascona.api;

import java.awt.Dimension;
import java.util.List;

import ch.alpine.ascona.util.dis.GeodesicDisplayRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.bridge.win.AbstractDemo;
import ch.alpine.bridge.win.BaseFrame;
import ch.alpine.bridge.win.DemoInterface;

@ReflectionMarker
public abstract class AbstractGeodesicDisplayDemo extends AbstractDemo implements DemoInterface {
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

  public synchronized final void setGeodesicDisplay(ManifoldDisplay geodesicDisplay) {
    manifoldDisplaySpinner.setValue(geodesicDisplay);
  }

  public void addSpinnerListener(SpinnerListener<ManifoldDisplay> spinnerListener) {
    manifoldDisplaySpinner.addSpinnerListener(spinnerListener);
  }

  /** @return */
  public List<ManifoldDisplay> getManifoldDisplays() {
    return list;
  }

  @Override // from DemoInterface
  public final BaseFrame start() {
    return timerFrame;
  }
}
