// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
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

public class ControlPointsRender implements RenderInterface {
  /** mouse snaps 20 pixel to control points */
  private static final Scalar PIXEL_THRESHOLD = RealScalar.of(20.0);
  /** refined points */
  private static final Stroke STROKE = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  // ---
  public Tensor control = Tensors.empty();
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

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    if (!isPositioningEnabled())
      return;
    mouse = mouseSe2CState.get();
    if (isPositioningOngoing())
      control.set(mouse, min_index);
    else {
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
    if (asconaParam.drawControlPoints) {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  public final MouseAdapter mouseAdapter = new MouseAdapter() {
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
          if (!isPositioningOngoing() && asconaParam.addRemoveControlPoints) {
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
        if (asconaParam.addRemoveControlPoints) {
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
  private final AsconaParam asconaParam;
  private final Supplier<ManifoldDisplay> supplier;
  private final Supplier<Tensor> mouseSe2CState;
  private final Supplier<Tensor> model2Pixel;

  public ControlPointsRender(AsconaParam asconaParam, Supplier<ManifoldDisplay> supplier, //
      Supplier<Tensor> mouseSe2CState, //
      Supplier<Tensor> model2Pixel) {
    this.asconaParam = asconaParam;
    this.supplier = supplier;
    this.mouseSe2CState = mouseSe2CState;
    this.model2Pixel = model2Pixel;
    setMidpointIndicated(asconaParam.addRemoveControlPoints);
  }

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

  /** curve control points, or
   * scattered set mode
   * 
   * @param enabled */
  public final void setMidpointIndicated(boolean enabled) {
    midpointIndicated = enabled;
  }

  public final boolean isMidpointIndicated() {
    return midpointIndicated;
  }

  public final Scalar getPositioningThreshold() {
    return PIXEL_THRESHOLD.divide(Sqrt.FUNCTION.apply(Abs.of(Det.of(model2Pixel.get()))));
  }

  /** @param control points as matrix of dimensions N x 3 */
  public final void setControlPointsSe2(Tensor control) {
    min_index = null;
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

  public final ManifoldDisplay manifoldDisplay() {
    return supplier.get();
  }

  public static ControlPointsRender create(AsconaParam asconaParam, Supplier<ManifoldDisplay> supplier, GeometricComponent geometricComponent) {
    ControlPointsRender controlPointsRender = new ControlPointsRender(asconaParam, supplier, //
        geometricComponent::getMouseSe2CState, //
        geometricComponent::getModel2Pixel);
    geometricComponent.jComponent.addMouseListener(controlPointsRender.mouseAdapter);
    geometricComponent.jComponent.addMouseMotionListener(controlPointsRender.mouseAdapter);
    geometricComponent.addRenderInterface(controlPointsRender);
    return controlPointsRender;
  }
}
