// code by jph
package ch.alpine.ascona.decim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.api.CurveOperator;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophis.itp.UniformResample;
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

class S2DeltaDemo extends ManifoldDisplayDemo {
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "200", "500", "1000" })
    public Integer numel = 200;
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar angle = RealScalar.of(0.1);
    @FieldSlider
    @FieldClip(min = "0.01", max = "1")
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
    super(param = new Param());
    fieldsEditor(param).addUniversalListener(this::compute);
    compute();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.S2_ONLY;
  }

  private void compute() {
    ScalarTensorFunction stf = new S2Loxodrome(param.angle);
    Tensor domain = Subdivide.of(0, 20, param.numel);
    Tensor sequence = domain.maps(stf);
    CurveOperator curveOperator = UniformResample.of(SnManifold.INSTANCE, SnManifold.INSTANCE, param.delta);
    sequence = curveOperator.string(sequence);
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
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    new PathRender(ColorStroke.CURVE, Tensor.of(snDeltaRaw.sequence.stream().map(manifoldDisplay::point2xy)), false).render(geometricLayer, graphics);
    new PathRender(ColorStroke.SECONDARY_CURVE, Tensor.of(snDeltaFil.sequence.stream().map(manifoldDisplay::point2xy)), false).render(geometricLayer, graphics);
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
    Dimension dimension = getSize();
    int WIDTH = dimension.width / 4;
    int HEIGHT = dimension.height / 2;
    {
      int ofs = dimension.width / 2;
      snDeltaRaw.show1.render_autoIndent(graphics, new Rectangle(ofs, 0, WIDTH, HEIGHT));
      snDeltaRaw.shows[0].render_autoIndent(graphics, new Rectangle(ofs, HEIGHT, WIDTH, HEIGHT));
    }
    {
      int ofs = dimension.width * 3 / 4;
      snDeltaFil.show1.render_autoIndent(graphics, new Rectangle(ofs, 0, WIDTH, HEIGHT));
      snDeltaFil.shows[0].render_autoIndent(graphics, new Rectangle(ofs, HEIGHT, WIDTH, HEIGHT));
    }
  }

  static void main() {
    new S2DeltaDemo().runStandalone();
  }
}
