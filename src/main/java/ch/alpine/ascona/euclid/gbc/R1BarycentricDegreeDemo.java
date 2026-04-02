// code by jph
package ch.alpine.ascona.euclid.gbc;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.Collection;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.ColorStrokeIndexed;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.itp.BarycentricRationalInterpolation;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.col.ColorDataIndexed;
import ch.alpine.tensor.col.ColorDataLists;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.ply.InterpolatingPolynomial;

class R1BarycentricDegreeDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    public Boolean lagrange = true;
    @FieldClip(min = "0", max = "4")
    public Integer degree = 1;
  }

  private final Param param;

  public R1BarycentricDegreeDemo() {
    super(this.param = new Param());
    geometricComponent().addRenderInterfaceBackground(new GridRender(geometricComponent()::getSize));
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 1, 0}, {2, 2, 0}}"));
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  private static final Scalar MARGIN = RealScalar.TWO;

  static Tensor domain(Tensor support) {
    return Subdivide.of( //
        support.stream().reduce(Min::of).orElseThrow().add(MARGIN.negate()), //
        support.stream().reduce(Max::of).orElseThrow().add(MARGIN), 128).maps(N.DOUBLE);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = Sort.of(getGeodesicControlPoints());
    if (1 < control.length()) {
      Tensor support = control.get(Tensor.ALL, 0);
      Tensor funceva = control.get(Tensor.ALL, 1);
      // ---
      Tensor domain = domain(support);
      if (param.lagrange) {
        ScalarTensorFunction geodesicNeville = InterpolatingPolynomial.of(support).scalarTensorFunction(funceva);
        Tensor basis = domain.maps(geodesicNeville);
        {
          Tensor curve = Transpose.of(Tensors.of(domain, basis));
          new PathRender(ColorStroke.SECONDARY_CURVE, curve, false).render(geometricLayer, graphics);
        }
      }
      // ---
      ScalarTensorFunction stf = BarycentricRationalInterpolation.of(support, param.degree);
      Tensor basis = domain.maps(stf);
      {
        Tensor curve = Transpose.of(Tensors.of(domain, basis.dot(funceva)));
        new PathRender(ColorStroke.CURVE, curve, false).render(geometricLayer, graphics);
      }
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.cyclic();
      ColorStrokeIndexed colorStrokeIndexed = new ColorStrokeIndexed(colorDataIndexed, new BasicStroke());
      for (int index = 0; index < funceva.length(); ++index) {
        Tensor curve = Transpose.of(Tensors.of(domain, basis.get(Tensor.ALL, index)));
        new PathRender(colorStrokeIndexed.getColorStroke(index), curve, false).render(geometricLayer, graphics);
      }
    }
  }

  static void main() {
    new R1BarycentricDegreeDemo().runStandalone();
  }
}
