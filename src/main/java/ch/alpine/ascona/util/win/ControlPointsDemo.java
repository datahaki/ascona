// code by jph, gjoel
package ch.alpine.ascona.util.win;

import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JButton;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.ControlPointsRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

/** class is used in other projects outside of owl */
// TODO ASCONA possibly provide option for cyclic midpoint indication (see R2Bary..Coord..Demo)
public abstract class ControlPointsDemo extends AbstractDemo {
  public final ControlPointsRender controlPointsRender;
  private final AsconaParam asconaParam;

  @SafeVarargs
  protected ControlPointsDemo(Object... objects) {
    super(objects);
    this.asconaParam = (AsconaParam) objects[0];
    controlPointsRender = ControlPointsRender.create( //
        asconaParam, this::manifoldDisplay, timerFrame.geometricComponent);
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
    timerFrame.geometricComponent.addRenderInterface(controlPointsRender);
  }

  public AsconaParam asconaParam() {
    return asconaParam;
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
    fieldsEditor(0).updateJComponents();
  }

  @Deprecated
  public void addManifoldListener(SpinnerListener<ManifoldDisplays> spinnerListener) {
    fieldsEditor(0).addUniversalListener(() -> spinnerListener.spun(asconaParam.manifoldDisplays));
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
