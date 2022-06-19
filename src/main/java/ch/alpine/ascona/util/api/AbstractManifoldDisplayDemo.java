// code by jph
package ch.alpine.ascona.util.api;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;

@ReflectionMarker
public abstract class AbstractManifoldDisplayDemo extends AbstractDemo {
  public static final class MdParam {
    public ManifoldDisplays m;
  }

  private final MdParam mdParam;
  private final SpinnerLabel<ManifoldDisplays> manifoldDisplaySpinner;
  private final List<ManifoldDisplays> list;

  public AbstractManifoldDisplayDemo(List<ManifoldDisplays> list) {
    mdParam = new MdParam();
    // mdParam.m = list.get(0);
    if (list.isEmpty())
      throw new RuntimeException();
    this.list = list;
    manifoldDisplaySpinner = SpinnerLabel.of(list);
    manifoldDisplaySpinner.setValue(list.get(0));
    if (1 < list.size()) {
      manifoldDisplaySpinner.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "geodesic type");
      timerFrame.jToolBar.addSeparator();
    }
    timerFrame.geometricComponent.addRenderInterfaceBackground(new RenderInterface() {
      @Override
      public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
        manifoldDisplay().background().render(geometricLayer, graphics);
      }
    });
  }

  /** @return */
  public final ManifoldDisplay manifoldDisplay() {
    return manifoldDisplaySpinner.getValue().manifoldDisplay();
  }

  public synchronized final void setManifoldDisplay(ManifoldDisplays manifoldDisplay) {
    manifoldDisplaySpinner.setValue(manifoldDisplay);
  }

  public synchronized final void reportToAll() {
    manifoldDisplaySpinner.reportToAll();
  }

  public void addManifoldListener(SpinnerListener<ManifoldDisplays> spinnerListener) {
    manifoldDisplaySpinner.addSpinnerListener(spinnerListener);
  }

  /** @return */
  public List<ManifoldDisplays> getManifoldDisplays() {
    return list;
  }
}
