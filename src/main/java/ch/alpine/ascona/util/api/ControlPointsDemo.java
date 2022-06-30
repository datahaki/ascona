// code by jph, gjoel
package ch.alpine.ascona.util.api;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import ch.alpine.sophus.hs.r2.Extract2D;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Insert;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.mat.re.Det;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.pow.Sqrt;

/** class is used in other projects outside of owl */
@ReflectionMarker
// TODO ASCONA possibly create TABs for each Manifold Display (in order to leave ctrl points)
// TODO ASCONA possibly provide option for cyclic midpoint indication (see R2Bary..Coord..Demo)
// TODO ASCONA use LeversRender in control points render
public abstract class ControlPointsDemo extends AbstractDemo {
  /** mouse snaps 20 pixel to control points */
  private static final Scalar PIXEL_THRESHOLD = RealScalar.of(20.0);
  /** refined points */
  private static final Stroke STROKE = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  // ---
  private Tensor control = Tensors.empty();
  private Tensor mouse = Array.zeros(3);
  /** min_index is non-null while the user drags a control points */
  private Integer min_index = null;
  private boolean mousePositioning = true;
  private boolean midpointIndicated = true;
  // ---
  private final static Color ORANGE = new Color(255, 200, 0, 192);
  private final static Color GREEN = new Color(0, 255, 0, 192);

  private class Midpoints {
    private final ManifoldDisplay manifoldDisplay = manifoldDisplay();
    private final Tensor midpoints;
    private final int index;

    public Midpoints() {
      CurveSubdivision curveSubdivision = new ControlMidpoints(manifoldDisplay.geodesicSpace());
      midpoints = curveSubdivision.string(getGeodesicControlPoints());
      Tensor mouse_dist = Tensor.of(midpoints.stream() //
          .map(manifoldDisplay::point2xy) //
          .map(mouse.extract(0, 2)::subtract) //
          .map(Vector2Norm::of));
      ArgMinValue argMinValue = ArgMinValue.of(mouse_dist);
      index = argMinValue.index();
    }

    Tensor closestXY() {
      return manifoldDisplay.point2xy(midpoints.get(index));
    }
  }

  // ---
  private final RenderInterface renderInterface = new RenderInterface() {
    @Override
    public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
      if (!isPositioningEnabled())
        return;
      mouse = timerFrame.geometricComponent.getMouseSe2CState();
      if (isPositioningOngoing())
        control.set(mouse, min_index);
      else {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        final boolean hold;
        {
          Tensor mouse_dist = Tensor.of(control.stream() //
              .map(mouse::subtract) //
              .map(Extract2D.FUNCTION) //
              .map(Vector2Norm::of));
          ArgMinValue argMinValue = ArgMinValue.of(mouse_dist);
          Optional<Scalar> value = argMinValue.value(getPositioningThreshold());
          hold = value.isPresent() && isPositioningEnabled();
          graphics.setColor(hold ? ORANGE : GREEN);
          Tensor posit = mouse;
          if (hold) {
            graphics.setStroke(new BasicStroke(2f));
            Tensor closest = control.get(argMinValue.index());
            graphics.draw(geometricLayer.toPath2D(Tensors.of(mouse, closest)));
            graphics.setStroke(new BasicStroke());
            posit.set(closest.get(0), 0);
            posit.set(closest.get(1), 1);
          }
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(manifoldDisplay.xya2point(posit)));
          graphics.fill(geometricLayer.toPath2D(manifoldDisplay.shape()));
          geometricLayer.popMatrix();
        }
        if (!hold && Tensors.nonEmpty(control) && midpointIndicated) {
          graphics.setColor(Color.RED);
          graphics.setStroke(STROKE);
          graphics.draw(geometricLayer.toLine2D(mouse, new Midpoints().closestXY()));
          graphics.setStroke(new BasicStroke());
        }
      }
    }
  };
  private final AsconaParam asconaParam;
  private final FieldsEditor fieldsEditor;

  /** Hint: {@link #setPositioningEnabled(boolean)} controls positioning of control points
   * 
   * @param addRemoveControlPoints whether the number of control points is variable
   * @param list */
  public ControlPointsDemo(boolean addRemoveControlPoints, List<ManifoldDisplays> list) {
    asconaParam = new AsconaParam(list);
    // TODO ASCONA only if list > 1
    fieldsEditor = ToolbarFieldsEditor.add(asconaParam.spaceParam, timerFrame.jToolBar);
    timerFrame.jToolBar.addSeparator();
    timerFrame.geometricComponent.addRenderInterfaceBackground(new RenderInterface() {
      @Override
      public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
        manifoldDisplay().background().render(geometricLayer, graphics);
      }
    });
    // ---
    if (addRemoveControlPoints) {
      ActionListener actionListener = actionEvent -> {
        min_index = null;
        control = Tensors.empty();
      };
      JButton jButton = new JButton("clear");
      jButton.addActionListener(actionListener);
      timerFrame.jToolBar.add(jButton);
    } else {
      setMidpointIndicated(false);
    }
    MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (!isPositioningEnabled())
          return;
        switch (mouseEvent.getButton()) {
        case MouseEvent.BUTTON1:
          if (isPositioningOngoing()) {
            min_index = null; // release
            // released();
          } else {
            {
              Tensor mouse_dist = Tensor.of(control.stream() //
                  .map(mouse::subtract) //
                  .map(Extract2D.FUNCTION) //
                  .map(Vector2Norm::of));
              ArgMinValue argMinValue = ArgMinValue.of(mouse_dist);
              min_index = argMinValue.index(getPositioningThreshold()).orElse(null);
            }
            if (!isPositioningOngoing() && addRemoveControlPoints) {
              // insert
              if (control.length() < 2 || !isMidpointIndicated()) {
                control = control.append(mouse);
                min_index = control.length() - 1;
              } else {
                Midpoints midpoints = new Midpoints();
                control = Insert.of(control, mouse, midpoints.index);
                min_index = midpoints.index;
              }
            }
          }
          break;
        case MouseEvent.BUTTON3: // remove point
          if (addRemoveControlPoints) {
            if (!isPositioningOngoing()) {
              Tensor mouse_dist = Tensor.of(control.stream() //
                  .map(mouse::subtract) //
                  .map(Extract2D.FUNCTION) //
                  .map(Vector2Norm::of));
              ArgMinValue argMinValue = ArgMinValue.of(mouse_dist);
              min_index = argMinValue.index(getPositioningThreshold()).orElse(null);
            }
            if (isPositioningOngoing()) {
              control = Join.of(control.extract(0, min_index), control.extract(min_index + 1, control.length()));
              min_index = null;
            }
          }
          break;
        default:
        }
      }
    };
    // ---
    timerFrame.geometricComponent.jComponent.addMouseListener(mouseAdapter);
    timerFrame.geometricComponent.jComponent.addMouseMotionListener(mouseAdapter);
    timerFrame.geometricComponent.addRenderInterface(renderInterface);
  }

  /** function is called when mouse is released */
  // public void released() {
  // API needs comments and better naming
  // }
  /** when positioning is disabled, the mouse position is not indicated graphically
   * 
   * @param enabled */
  public final void setPositioningEnabled(boolean enabled) {
    if (!enabled)
      min_index = null;
    mousePositioning = enabled;
  }

  /** @return */
  public final boolean isPositioningEnabled() {
    return mousePositioning;
  }

  /** @return whether user is currently dragging a control point */
  public final boolean isPositioningOngoing() {
    return Objects.nonNull(min_index);
  }

  public final void setMidpointIndicated(boolean enabled) {
    midpointIndicated = enabled;
  }

  public final boolean isMidpointIndicated() {
    return midpointIndicated;
  }

  public final Scalar getPositioningThreshold() {
    return PIXEL_THRESHOLD.divide(Sqrt.FUNCTION.apply(Abs.of(Det.of(timerFrame.geometricComponent.getModel2Pixel()))));
  }

  // TODO ASCONA API function should not be here!
  public final void addButtonDubins() {
    JButton jButton = new JButton("dubins");
    jButton.setToolTipText("project control points to dubins path");
    jButton.addActionListener(actionEvent -> setControlPointsSe2(DubinsGenerator.project(control)));
    timerFrame.jToolBar.add(jButton);
  }

  /** @param control points as matrix of dimensions N x 3 */
  public final void setControlPointsSe2(Tensor control) {
    this.control = Tensor.of(control.stream() //
        .map(row -> VectorQ.requireLength(row, 3).map(Tensor::copy)));
  }

  /** @return control points as matrix of dimensions N x 3 */
  public final Tensor getControlPointsSe2() {
    return control.unmodifiable(); // TODO ASCONA API should return copy!?
  }

  /** @return control points for selected {@link ManifoldDisplay} */
  public final Tensor getGeodesicControlPoints() {
    return getGeodesicControlPoints(0, Integer.MAX_VALUE);
  }

  /** @param skip
   * @param maxSize
   * @return */
  public final Tensor getGeodesicControlPoints(int skip, int maxSize) {
    return Tensor.of(control.stream() //
        .skip(skip) //
        .limit(maxSize) //
        .map(manifoldDisplay()::xya2point) //
        .map(N.DOUBLE::of));
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
}
