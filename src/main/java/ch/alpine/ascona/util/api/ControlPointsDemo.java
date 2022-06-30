// code by jph, gjoel
package ch.alpine.ascona.util.api;

import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JButton;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.tensor.Tensor;

/** class is used in other projects outside of owl */
@ReflectionMarker
// TODO ASCONA possibly create TABs for each Manifold Display (in order to leave ctrl points)
// TODO ASCONA possibly provide option for cyclic midpoint indication (see R2Bary..Coord..Demo)
// TODO ASCONA use LeversRender in control points render
public abstract class ControlPointsDemo extends AbstractDemo {
  public final ControlPointsRender renderInterface;
  private final AsconaParam asconaParam;
  private final FieldsEditor fieldsEditor;

  /** Hint: {@link #setPositioningEnabled(boolean)} controls positioning of control points
   * 
   * @param addRemoveControlPoints whether the number of control points is variable
   * @param list */
  public ControlPointsDemo(boolean addRemoveControlPoints, List<ManifoldDisplays> list) {
    asconaParam = new AsconaParam(list);
    renderInterface = new ControlPointsRender(addRemoveControlPoints, this::manifoldDisplay, //
        timerFrame.geometricComponent::getMouseSe2CState, //
        timerFrame.geometricComponent::getModel2Pixel);
    // TODO ASCONA only if list > 1
    fieldsEditor = ToolbarFieldsEditor.add(asconaParam.spaceParam, timerFrame.jToolBar);
    timerFrame.jToolBar.addSeparator();
    if (addRemoveControlPoints) {
      JButton jButton = new JButton("clear");
      jButton.addActionListener(renderInterface.actionListener);
      timerFrame.jToolBar.add(jButton);
    }
    timerFrame.geometricComponent.addRenderInterfaceBackground(new RenderInterface() {
      @Override
      public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
        manifoldDisplay().background().render(geometricLayer, graphics);
      }
    });
    // ---
    // ---
    timerFrame.geometricComponent.jComponent.addMouseListener(renderInterface.mouseAdapter);
    timerFrame.geometricComponent.jComponent.addMouseMotionListener(renderInterface.mouseAdapter);
    timerFrame.geometricComponent.addRenderInterface(renderInterface);
  }

  // TODO ASCONA API function should not be here!
  public final void addButtonDubins() {
    JButton jButton = new JButton("dubins");
    jButton.setToolTipText("project control points to dubins path");
    jButton.addActionListener(actionEvent -> renderInterface.setControlPointsSe2(DubinsGenerator.project(renderInterface.control)));
    timerFrame.jToolBar.add(jButton);
  }

  /** @return */
  public final ManifoldDisplay manifoldDisplay() {
    return asconaParam.spaceParam.manifoldDisplays.manifoldDisplay();
  }

  public synchronized final void setManifoldDisplay(ManifoldDisplays manifoldDisplays) {
    asconaParam.spaceParam.manifoldDisplays = manifoldDisplays;
    fieldsEditor.updateJComponents();
  }

  public synchronized final void reportToAll() {
    // TODO ASCONA
    // fieldsEditor.reportToAll();
  }

  public void addManifoldListener(SpinnerListener<ManifoldDisplays> spinnerListener) {
    fieldsEditor.addUniversalListener(() -> spinnerListener.spun(asconaParam.spaceParam.manifoldDisplays));
  }

  /** @return */
  public List<ManifoldDisplays> getManifoldDisplays() {
    return asconaParam.spaceParam.getList();
  }

  /** @param control points as matrix of dimensions N x 3 */
  public final void setControlPointsSe2(Tensor control) {
    renderInterface.setControlPointsSe2(control);
  }

  /** @return control points as matrix of dimensions N x 3 */
  public final Tensor getControlPointsSe2() {
    return renderInterface.getControlPointsSe2();
  }

  /** @return control points for selected {@link ManifoldDisplay} */
  public final Tensor getGeodesicControlPoints() {
    return renderInterface.getGeodesicControlPoints();
  }
}
