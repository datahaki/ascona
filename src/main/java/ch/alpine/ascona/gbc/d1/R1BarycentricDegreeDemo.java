// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.itp.BarycentricRationalInterpolation;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.itp.InterpolatingPolynomial;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.N;

public class R1BarycentricDegreeDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public Boolean lagrange = true;
    @FieldInteger
    @FieldClip(min = "0", max = "4")
    public Scalar degree = RealScalar.ONE;
  }

  private final Param param;

  public R1BarycentricDegreeDemo() {
    this(new Param());
  }

  public R1BarycentricDegreeDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 1, 0}, {2, 2, 0}}"));
  }

  private static final Scalar MARGIN = RealScalar.of(2);

  static Tensor domain(Tensor support) {
    return Subdivide.of( //
        support.stream().reduce(Min::of).orElseThrow().add(MARGIN.negate()), //
        support.stream().reduce(Max::of).orElseThrow().add(MARGIN), 128).map(N.DOUBLE);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    // ---
    Tensor control = Sort.of(getGeodesicControlPoints());
    if (1 < control.length()) {
      Tensor support = control.get(Tensor.ALL, 0);
      Tensor funceva = control.get(Tensor.ALL, 1);
      // ---
      Tensor domain = domain(support);
      if (param.lagrange) {
        ScalarTensorFunction geodesicNeville = InterpolatingPolynomial.of(manifoldDisplay.geodesicSpace(), support).scalarTensorFunction(funceva);
        Tensor basis = domain.map(geodesicNeville);
        {
          Tensor curve = Transpose.of(Tensors.of(domain, basis));
          new PathRender(new Color(255, 0, 0, 128), STROKE).setCurve(curve, false).render(geometricLayer, graphics);
        }
      }
      // ---
      ScalarTensorFunction scalarTensorFunction = //
          BarycentricRationalInterpolation.of(support, param.degree.number().intValue());
      Tensor basis = domain.map(scalarTensorFunction);
      {
        Tensor curve = Transpose.of(Tensors.of(domain, basis.dot(funceva)));
        new PathRender(Color.BLUE, 1.25f).setCurve(curve, false).render(geometricLayer, graphics);
      }
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.cyclic();
      for (int index = 0; index < funceva.length(); ++index) {
        Color color = colorDataIndexed.getColor(index);
        Tensor curve = Transpose.of(Tensors.of(domain, basis.get(Tensor.ALL, index)));
        new PathRender(color, 1f).setCurve(curve, false).render(geometricLayer, graphics);
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
