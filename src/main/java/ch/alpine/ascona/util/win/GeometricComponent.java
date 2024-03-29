// code by jph
package ch.alpine.ascona.util.win;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.awt.AwtUtil;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.mat.re.Det;
import ch.alpine.tensor.mat.re.LinearSolve;
import ch.alpine.tensor.qty.Degree;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.Sign;
import ch.alpine.tensor.sca.pow.Power;
import ch.alpine.tensor.sca.pow.Sqrt;
import ch.alpine.tensor.sca.tri.ArcTan;

public final class GeometricComponent {
  private static final Scalar SCALE_FACTOR = Sqrt.FUNCTION.apply(RealScalar.TWO);
  private static final Font FONT_DEFAULT = new Font(Font.DIALOG, Font.PLAIN, 12);
  private static final Scalar WHEEL_ANGLE = Degree.of(15);
  /** initial model to pixel matrix */
  private static final Tensor MODEL2PIXEL_INITIAL = Tensors.matrix(new Number[][] { //
      { 60, 0, 300 }, //
      { 0, -60, 300 }, //
      { 0, 0, 1 }, //
  }).unmodifiable();
  // ---
  /** public access to final JComponent: attach mouse listeners, get/set properties, ... */
  public final JComponent jComponent = new JComponent() {
    private final IntervalClock intervalClock = new IntervalClock();

    @Override
    protected void paintComponent(Graphics _g) {
      render((Graphics2D) _g, getSize());
      // display frame rate only when rendering in component
      Graphics graphics = _g.create();
      graphics.setFont(FONT_DEFAULT);
      graphics.setColor(Color.LIGHT_GRAY);
      graphics.setClip(null);
      graphics.drawString(String.format("%3.1f Hz", intervalClock.hertz()), 0, 10);
      graphics.dispose();
    }
  };
  // TODO ASCONA possibly use EnumMultimap with background... main .. hud
  private final List<RenderInterface> renderBackground = new CopyOnWriteArrayList<>();
  private final List<RenderInterface> renderInterfaces = new CopyOnWriteArrayList<>();
  // ---
  /** 3x3 affine matrix that maps model to pixel coordinates */
  private Tensor model2pixel = MODEL2PIXEL_INITIAL.copy();
  private Tensor mouseLocation = Array.zeros(2);
  private int mouseWheel = 0;
  private boolean isZoomable = true;
  private boolean isRotatable = true;
  // private boolean printPositionOnClick = true;
  private int buttonDrag = MouseEvent.BUTTON3;

  public GeometricComponent() {
    jComponent.addMouseWheelListener(event -> {
      final int delta = -event.getWheelRotation(); // either 1 or -1
      final int mods = event.getModifiersEx();
      final int mask = InputEvent.CTRL_DOWN_MASK; // 128 = 2^7
      if ((mods & mask) == 0) // ctrl pressed?
        mouseWheel += delta;
      else //
      if (isZoomable) {
        Scalar factor = Power.of(SCALE_FACTOR, delta);
        Tensor scale = DiagonalMatrix.of(factor, factor, RealScalar.ONE);
        Tensor shift = Tensors.vector(event.getX(), event.getY());
        shift = shift.subtract(shift.multiply(factor));
        scale.set(shift.Get(0), 0, 2);
        scale.set(shift.Get(1), 1, 2);
        model2pixel = scale.dot(model2pixel);
      }
      jComponent.repaint();
    });
    {
      MouseInputListener mouseInputListener = new MouseInputAdapter() {
        private Point down = null;
        private Tensor center = null;

        @Override
        public void mouseMoved(MouseEvent mouseEvent) {
          mouseLocation = toModel(mouseEvent.getPoint());
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
          if (mouseEvent.getButton() == buttonDrag) {
            down = mouseEvent.getPoint();
            Dimension dimension = jComponent.getSize();
            center = toModel(AwtUtil.center(dimension)).unmodifiable();
          }
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
          mouseLocation = toModel(mouseEvent.getPoint());
          if (Objects.nonNull(down)) {
            Point now = mouseEvent.getPoint();
            int dx = now.x - down.x;
            int dy = now.y - down.y;
            // ---
            Dimension dimension = jComponent.getSize();
            Scalar a1 = ArcTan.of(now.x - dimension.width / 2, now.y - dimension.height / 2);
            Scalar a2 = ArcTan.of(down.x - dimension.width / 2, down.y - dimension.height / 2);
            // ---
            down = now;
            final int mods = mouseEvent.getModifiersEx();
            final int mask = InputEvent.CTRL_DOWN_MASK; // 128 = 2^7
            if ((mods & mask) == 0 || !isRotatable) {
              model2pixel.set(RealScalar.of(dx)::add, 0, 2);
              model2pixel.set(RealScalar.of(dy)::add, 1, 2);
            } else {
              Tensor t1 = GfxMatrix.translation(center.negate());
              Tensor t2 = GfxMatrix.of(Append.of(center, a2.subtract(a1)));
              model2pixel = model2pixel.dot(t2).dot(t1);
            }
            jComponent.repaint();
          }
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
          down = null;
          center = null;
        }
      };
      jComponent.addMouseMotionListener(mouseInputListener);
      jComponent.addMouseListener(mouseInputListener);
    }
  }

  /** determines if mouseDragged + ctrl allows rotation
   * 
   * @param isRotatable */
  public void setRotatable(boolean isRotatable) {
    this.isRotatable = isRotatable;
  }

  /** determines if mouse wheel + ctrl change magnification
   * 
   * @param isZoomable */
  public void setZoomable(boolean isZoomable) {
    this.isZoomable = isZoomable;
  }

  /** @param button for instance MouseEvent.BUTTON1 */
  public void setButtonDrag(int button) {
    buttonDrag = button;
  }

  /** function only clears render interfaces in the foreground.
   * the background is unchanged
   * 
   * @param collection */
  public void setRenderInterfaces(Collection<RenderInterface> collection) {
    renderInterfaces.clear();
    renderInterfaces.addAll(collection);
  }

  public void addRenderInterface(RenderInterface renderInterface) {
    renderInterfaces.add(renderInterface);
  }

  /** @return {px, py, angle} in model space */
  public Tensor getMouseSe2CState() {
    Scalar scalar = RealScalar.of(mouseWheel).multiply(WHEEL_ANGLE);
    return Append.of(mouseLocation, scalar);
  }

  public void addRenderInterfaceBackground(RenderInterface renderInterface) {
    renderBackground.add(renderInterface);
  }

  // ---
  /** @param model2pixel with dimensions 3 x 3
   * @throws Exception if determinant of matrix is positive */
  public void setModel2Pixel(Tensor model2pixel) {
    this.model2pixel = model2pixel.copy(); // set matrix regardless of conditions
    // ---
    Scalar det = Det.of(model2pixel);
    if (Chop._08.isZero(det))
      System.err.println("model2pixel must not be singular");
    Sign.requirePositive(det.negate());
  }

  public Tensor getModel2Pixel() {
    return model2pixel.copy();
  }

  /** @param pix
   * @param piy */
  public void setOffset(int pix, int piy) {
    model2pixel.set(RealScalar.of(pix), 0, 2);
    model2pixel.set(RealScalar.of(piy), 1, 2);
  }

  void render(Graphics2D graphics, Dimension dimension) {
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, dimension.width, dimension.height);
    // ---
    GeometricLayer geometricLayer = new GeometricLayer(model2pixel);
    renderBackground.forEach(renderInterface -> renderInterface.render(geometricLayer, graphics));
    renderInterfaces.forEach(renderInterface -> renderInterface.render(geometricLayer, graphics));
  }

  // ---
  /** transforms point in pixel space to coordinates of model space
   * 
   * @param point
   * @return tensor of length 2 */
  private Tensor toModel(Point point) {
    return LinearSolve.of(model2pixel, Tensors.vector(point.x, point.y, 1)).extract(0, 2);
  }
}
