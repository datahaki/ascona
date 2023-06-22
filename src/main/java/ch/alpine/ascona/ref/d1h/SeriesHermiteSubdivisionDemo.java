// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascona.util.api.HermiteSubdivisions;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.ren.Curvature2DRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.r2.Extract2D;
import ch.alpine.sophus.math.Do;
import ch.alpine.sophus.math.api.TensorIteration;
import ch.alpine.sophus.ref.d1h.HermiteSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.chq.FiniteTensorQ;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.ply.Polynomial;

public class SeriesHermiteSubdivisionDemo extends AbstractDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  @ReflectionMarker
  public static class Param {
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
    this(new Param());
  }

  public SeriesHermiteSubdivisionDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(this::compute);
    compute();
  }

  Tensor _control = Tensors.empty();
  Tensor geo_ctrl = Tensors.empty();

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = R2Display.INSTANCE;
    RenderQuality.setQuality(graphics);
    if (1 < _control.length()) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      HermiteSubdivision hermiteSubdivision = param.scheme.supply(homogeneousSpace);
      Tensor control = _control.map(N.DOUBLE);
      Scalar delta = RealScalar.ONE;
      TensorIteration tensorIteration = hermiteSubdivision.string(delta, control);
      int levels = param.refine;
      Tensor iterate = Do.of(control, tensorIteration::iterate, levels);
      Tensor curve = Tensor.of(iterate.get(Tensor.ALL, 0).stream().map(Extract2D.FUNCTION));
      Curvature2DRender.of(curve, false).render(geometricLayer, graphics);
      // ---
      if (param.derivatives) {
        Tensor deltas = iterate.get(Tensor.ALL, 1);
        if (0 < deltas.length()) {
          Show show = StaticHelper.listPlot(deltas, delta, levels);
          Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
          show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
        }
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, geo_ctrl, null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  private void compute() {
    Tensor _coeffs = param.coeffs;
    if (VectorQ.of(_coeffs) && //
        FiniteTensorQ.of(_coeffs)) {
      Polynomial f0 = Polynomial.of(_coeffs);
      ScalarUnaryOperator f1 = f0.derivative();
      Tensor vx0 = Range.of(-4, 5);
      Tensor vd0 = vx0.map(f0);
      Tensor vx1 = ConstantArray.of(RealScalar.ONE, vx0.length());
      Tensor vd1 = vx0.map(f1);
      Tensor p0 = Transpose.of(Tensors.of(vx0, vd0));
      Tensor p1 = Transpose.of(Tensors.of(vx1, vd1));
      _control = Transpose.of(Tensors.of(p0, p1));
      geo_ctrl = Tensor.of(p0.stream().map(Tensor::copy));
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
