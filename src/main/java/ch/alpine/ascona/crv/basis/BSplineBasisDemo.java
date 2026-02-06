// code by jph
package ch.alpine.ascona.crv.basis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.GeodesicBSplineFunction;
import ch.alpine.sophus.lie.rn.RGroup;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.itp.BSplineInterpolation;
import ch.alpine.tensor.mat.re.Inverse;

public class BSplineBasisDemo extends ControlPointsDemo {
  private static final Color TICKS_COLOR = new Color(0, 0, 0, 128);

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public Integer degree = 1;
    @FieldSlider
    @FieldClip(min = "0", max = "8")
    public Integer refine = 4;
    public Boolean interp = false;
    public ColorDataLists cdl = ColorDataLists._097;
  }

  private final Param param;

  public BSplineBasisDemo() {
    this(new Param());
  }

  public BSplineBasisDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 0, 0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    int _degree = param.degree;
    int _levels = param.refine;
    Tensor control = getGeodesicControlPoints();
    {
      graphics.setStroke(new BasicStroke(1.25f));
      Tensor matrix = geometricLayer.getMatrix();
      geometricLayer.pushMatrix(Inverse.of(matrix));
      {
        ColorDataIndexed colorDataIndexed = param.cdl.cyclic().deriveWithAlpha(192);
        for (int length = 2; length <= 8; ++length) {
          Tensor string = Tensors.fromString("{{100, 0, 0}, {0, -100, 0}, {0, 0, 1}}");
          string.set(RealScalar.of(110 * length), 1, 2);
          geometricLayer.pushMatrix(string);
          for (int k_th = 0; k_th < length; ++k_th) {
            GeodesicBSplineFunction bSplineFunction = //
                GeodesicBSplineFunction.of(RGroup.INSTANCE, _degree, UnitVector.of(length, k_th));
            Tensor domain = Subdivide.of(0, length - 1, 100);
            Tensor values = domain.map(bSplineFunction);
            Tensor tensor = Transpose.of(Tensors.of(domain, values));
            graphics.setColor(colorDataIndexed.getColor(k_th));
            graphics.draw(geometricLayer.toPath2D(tensor));
            graphics.setColor(TICKS_COLOR);
            graphics.draw(geometricLayer.toPath2D(Tensors.matrix(new Number[][] { { k_th, 0 }, { k_th, .1 } })));
          }
          geometricLayer.popMatrix();
        }
      }
      geometricLayer.popMatrix();
      graphics.setStroke(new BasicStroke(1f));
    }
    // ---
    Tensor effective = param.interp //
        ? BSplineInterpolation.solve(_degree, control)
        : control;
    GeodesicBSplineFunction bSplineFunction = //
        GeodesicBSplineFunction.of(RGroup.INSTANCE, _degree, effective);
    Tensor refined = Subdivide.of(0, effective.length() - 1, 4 << _levels).map(bSplineFunction);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    Curvature2DRender.of(refined, false).render(geometricLayer, graphics);
  }

  static void main() {
    launch();
  }
}
