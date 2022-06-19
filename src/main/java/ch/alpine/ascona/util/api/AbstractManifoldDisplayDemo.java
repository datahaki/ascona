// code by jph
package ch.alpine.ascona.util.api;

import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.SpinnerListener;

@ReflectionMarker
public abstract class AbstractManifoldDisplayDemo extends AbstractDemo {
  @ReflectionMarker
  public static class MdParam {
    private final List<ManifoldDisplays> list;
    @FieldSelectionCallback("getList")
    public ManifoldDisplays manifoldDisplays;

    public MdParam(List<ManifoldDisplays> list) {
      this.list = list;
    }

    public List<ManifoldDisplays> getList() {
      return list;
    }
  }

  private final MdParam mdParam;
  private final FieldsEditor fieldsEditor;

  public AbstractManifoldDisplayDemo(List<ManifoldDisplays> list) {
    mdParam = new MdParam(list);
    mdParam.manifoldDisplays = list.get(0);
    // this.= list;
    fieldsEditor = ToolbarFieldsEditor.add(mdParam, timerFrame.jToolBar);
    timerFrame.jToolBar.addSeparator();
    timerFrame.geometricComponent.addRenderInterfaceBackground(new RenderInterface() {
      @Override
      public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
        manifoldDisplay().background().render(geometricLayer, graphics);
      }
    });
  }

  /** @return */
  public final ManifoldDisplay manifoldDisplay() {
    return mdParam.manifoldDisplays.manifoldDisplay();
  }

  public synchronized final void setManifoldDisplay(ManifoldDisplays manifoldDisplays) {
    mdParam.manifoldDisplays = manifoldDisplays;
    fieldsEditor.updateJComponents();
  }

  public synchronized final void reportToAll() {
    // TODO ASCONA
    // fieldsEditor.reportToAll();
  }

  public void addManifoldListener(SpinnerListener<ManifoldDisplays> spinnerListener) {
    fieldsEditor.addUniversalListener(() -> spinnerListener.actionPerformed(mdParam.manifoldDisplays));
  }

  /** @return */
  public List<ManifoldDisplays> getManifoldDisplays() {
    return mdParam.getList();
  }
}
