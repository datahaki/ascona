// code by jph
package ch.alpine.ascona.decim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.S2Display;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophis.itp.UniformResample;
import ch.alpine.sophis.ref.d1.CurveSubdivision;
import ch.alpine.sophus.hs.s.S2Loxodrome;
import ch.alpine.sophus.hs.s.SnManifold;
import ch.alpine.sophus.hs.s.SnRotationMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.win.WindowFunctions;

public class S2DeltaDemo extends AbstractDemo {
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 128 + 64);
  private static final Color COLOR_SHAPE = new Color(128, 255, 128, 128 + 64);
  private static final int WIDTH = 360;
  private static final int HEIGHT = 240;
  // ---
  private final PathRender pathRenderCurve = new PathRender(COLOR_CURVE);
  private final PathRender pathRenderShape = new PathRender(COLOR_SHAPE);
  // ---

  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar angle = RealScalar.of(0.1);
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar delta = RealScalar.of(0.1);
    @FieldSlider
    @FieldClip(min = "0", max = "0.1")
    public Scalar noise = RealScalar.of(0.01);
    @FieldClip(min = "1", max = "11")
    public Integer width = 5;
    public WindowFunctions f_window = WindowFunctions.FLAT_TOP;
    public WindowFunctions s_window = WindowFunctions.HANN;
    public Boolean differences = false;
    public Boolean transport = false;

    public int getWidth() {
      return 2 * width + 1;
    }
  }

  private final Param param;
  private SnDeltaContainer snDeltaRaw;
  private SnDeltaContainer snDeltaFil;

  public S2DeltaDemo() {
    this(new Param());
  }

  public S2DeltaDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(this::compute);
    compute();
  }

  private void compute() {
    ScalarTensorFunction stf = S2Loxodrome.of(param.angle);
    Tensor domain = Subdivide.of(0, 20, 200);
    CurveSubdivision curveSubdivision = UniformResample.of(SnManifold.INSTANCE, SnManifold.INSTANCE, param.delta);
    Tensor sequence = domain.maps(stf);
    sequence = curveSubdivision.string(sequence);
    TensorUnaryOperator tuo = t -> t; // SnPerturbation.of(NormalDistribution.of(param.noise.zero(), param.noise));
    sequence = Tensor.of(sequence.stream().map(tuo));
    ScalarUnaryOperator s_window = param.s_window.get();
    snDeltaRaw = new SnDeltaContainer(sequence, s_window);
    TensorUnaryOperator tensorUnaryOperator = new CenterFilter( //
        GeodesicCenter.of(SnManifold.INSTANCE, param.f_window.get()), param.getWidth());
    snDeltaFil = new SnDeltaContainer(tensorUnaryOperator.apply(sequence), s_window);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = S2Display.INSTANCE;
    manifoldDisplay.background().render(geometricLayer, graphics);
    pathRenderCurve.setCurve(Tensor.of(snDeltaRaw.sequence.stream().map(manifoldDisplay::point2xy)), false).render(geometricLayer, graphics);
    pathRenderShape.setCurve(Tensor.of(snDeltaFil.sequence.stream().map(manifoldDisplay::point2xy)), false).render(geometricLayer, graphics);
    if (param.differences)
      for (Tensor ctrl : snDeltaRaw.differences) {
        Tensor p = ctrl.get(0); // point
        Tensor v = ctrl.get(1); // vector
        {
          graphics.setStroke(new BasicStroke(1.5f));
          graphics.setColor(Color.GRAY);
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(p));
          graphics.draw(geometricLayer.toLine2D(manifoldDisplay.tangentProjection(p).apply(v)));
          geometricLayer.popMatrix();
        }
      }
    if (param.transport) { // moving a single tangent vector along
      Tensor v0 = UnitVector.of(3, 1).multiply(RealScalar.of(0.5));
      for (int index = 1; index < snDeltaRaw.sequence.length(); ++index) {
        Tensor p = snDeltaRaw.sequence.get(index - 1);
        {
          graphics.setStroke(new BasicStroke(1.5f));
          graphics.setColor(Color.RED);
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(p));
          graphics.draw(geometricLayer.toLine2D(manifoldDisplay.tangentProjection(p).apply(v0)));
          geometricLayer.popMatrix();
        }
        Tensor q = snDeltaRaw.sequence.get(index - 0);
        v0 = SnRotationMatrix.of(p, q).dot(v0);
      }
    }
    {
      int ofs = 0;
      snDeltaRaw.show1.render_autoIndent(graphics, new Rectangle(ofs, 0, WIDTH, HEIGHT));
      snDeltaRaw.shows[0].render_autoIndent(graphics, new Rectangle(ofs, HEIGHT, WIDTH, HEIGHT));
    }
    {
      int ofs = WIDTH;
      snDeltaFil.show1.render_autoIndent(graphics, new Rectangle(ofs, 0, WIDTH, HEIGHT));
      snDeltaFil.shows[0].render_autoIndent(graphics, new Rectangle(ofs, HEIGHT, WIDTH, HEIGHT));
    }
  }

  static void main() {
    new S2DeltaDemo().runStandalone();
  }
}
