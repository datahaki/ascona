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
import ch.alpine.tensor.Tensors;

/** class is used in other projects outside of owl */
@ReflectionMarker
// TODO ASCONA possibly create TABs for each Manifold Display (in order to leave ctrl points)
// TODO ASCONA possibly provide option for cyclic midpoint indication (see R2Bary..Coord..Demo)
public abstract class ControlPointsDemo extends AbstractDemo {
  public final ControlPointsRender controlPointsRender;
  private final AsconaParam asconaParam;

  /** Hint: {@link #setPositioningEnabled(boolean)} controls positioning of control points
   * 
   * @param addRemoveControlPoints whether the number of control points is variable
   * @param list */
  @Deprecated
  public ControlPointsDemo(boolean addRemoveControlPoints, List<ManifoldDisplays> list) {
    this(new AsconaParam(addRemoveControlPoints, list));
  }

  public ControlPointsDemo(AsconaParam asconaParam) {
    super(asconaParam);
    this.asconaParam = asconaParam;
    controlPointsRender = ControlPointsRenders.create( //
        asconaParam.addRemoveControlPoints, this::manifoldDisplay, timerFrame.geometricComponent);
    timerFrame.jToolBar.addSeparator();
    if (asconaParam.addRemoveControlPoints) {
      JButton jButton = new JButton("clear");
      jButton.addActionListener(e -> controlPointsRender.setControlPointsSe2(Tensors.empty()));
      timerFrame.jToolBar.add(jButton);
    }
    timerFrame.geometricComponent.addRenderInterfaceBackground(new RenderInterface() {
      @Override
      public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
        manifoldDisplay().background().render(geometricLayer, graphics);
      }
    });
  }

  // TODO ASCONA API function should not be here!
  public final void addButtonDubins() {
    JButton jButton = new JButton("dubins");
    jButton.setToolTipText("project control points to dubins path");
    jButton.addActionListener(actionEvent -> controlPointsRender.setControlPointsSe2(DubinsGenerator.project(controlPointsRender.control)));
    timerFrame.jToolBar.add(jButton);
  }

  /** @return */
  public final ManifoldDisplay manifoldDisplay() {
    return asconaParam.manifoldDisplays.manifoldDisplay();
  }

  public synchronized final void setManifoldDisplay(ManifoldDisplays manifoldDisplays) {
    asconaParam.manifoldDisplays = manifoldDisplays;
    fieldsEditor.updateJComponents();
  }

  public void addManifoldListener(SpinnerListener<ManifoldDisplays> spinnerListener) {
    fieldsEditor.addUniversalListener(() -> spinnerListener.spun(asconaParam.manifoldDisplays));
  }

  /** @return */
  public List<ManifoldDisplays> getManifoldDisplays() {
    return asconaParam.getList();
  }

  /** @param control points as matrix of dimensions N x 3 */
  public final void setControlPointsSe2(Tensor control) {
    controlPointsRender.setControlPointsSe2(control);
  }

  /** @return control points as matrix of dimensions N x 3 */
  public final Tensor getControlPointsSe2() {
    return controlPointsRender.getControlPointsSe2();
  }

  /** @return control points for selected {@link ManifoldDisplay} */
  public final Tensor getGeodesicControlPoints() {
    return controlPointsRender.getGeodesicControlPoints();
  }
}
