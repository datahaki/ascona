// code by jph
package ch.alpine.ascona.sub.d1h;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.math.Do;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.chq.FiniteTensorQ;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.ply.Polynomial;

class SeriesHermiteSubdivisionDemo extends EuclideanPlaneDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  @ReflectionMarker
  static class Param {
    public HermiteSubdivisions scheme = HermiteSubdivisions.HERMITE1;
    @FieldSlider
    @FieldPreferredWidth(100)
    @FieldClip(min = "0", max = "8")
    public Integer refine = 4;
    @FieldPreferredWidth(300)
    public Tensor coeffs = Tensors.fromString("{2, 1, -1/5, -1/10}");
    // ---
    public Boolean derivatives = true;
  }

  private final Param param;

  public SeriesHermiteSubdivisionDemo() {
    super(param = new Param());
    fieldsEditor(param).addUniversalListener(this::compute);
    compute();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  Tensor _control = Tensors.empty();

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    if (1 < _control.length()) {
      HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
      HermiteSubdivision hermiteSubdivision = param.scheme.supply(homogeneousSpace);
      Tensor control = _control.maps(N.DOUBLE);
      Scalar delta = RealScalar.ONE;
      TensorIteration tensorIteration = hermiteSubdivision.string(delta, control);
      int levels = param.refine;
      Tensor iterate = Do.of(control, tensorIteration::iterate, levels);
      Tensor positions = iterate.get(Tensor.ALL, 0);
      Tensor euclidXY = manifoldDisplay.point2xy().slash(positions);
      Curvature2DRender.of(euclidXY, false).render(geometricLayer, graphics);
      new PathRender(ColorStroke.CURVE, euclidXY, false).render(geometricLayer, graphics);
      // ---
      if (param.derivatives) {
        Tensor deltas = iterate.get(Tensor.ALL, 1);
        if (0 < deltas.length()) {
          Show show = StaticHelper.listPlot(deltas, delta, levels);
          Dimension dimension = getSize();
          show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
        }
      }
    }
  }

  private void compute() {
    Tensor _coeffs = param.coeffs;
    Tensor geo_ctrl = Tensors.empty();
    if (VectorQ.of(_coeffs) && //
        0 < _coeffs.length() && //
        FiniteTensorQ.of(_coeffs)) {
      Polynomial f0 = Polynomial.of(_coeffs);
      Polynomial f1 = f0.derivative();
      Tensor vx0 = Range.of(-4, 5);
      Tensor vd0 = vx0.maps(f0);
      Tensor vx1 = ConstantArray.of(RealScalar.ONE, vx0.length());
      Tensor vd1 = vx0.maps(f1);
      Tensor p0 = Transpose.of(Tensors.of(vx0, vd0));
      Tensor p1 = Transpose.of(Tensors.of(vx1, vd1));
      _control = Transpose.of(Tensors.of(p0, p1));
      geo_ctrl = Tensor.of(p0.stream().map(Tensor::copy));
    }
    setGeodesicControlPoints(geo_ctrl);
  }

  static void main() {
    new SeriesHermiteSubdivisionDemo().runStandalone();
  }
}
