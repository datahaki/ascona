// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;
import java.util.Arrays;

import ch.alpine.ascony.dis.ManifoldDisplay;
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
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public class NonuniformSplineDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public Integer degree = 1;
    @FieldSlider
    @FieldClip(min = "0", max = "12")
    public Integer refine = 4;
  }

  private final Param param;

  public NonuniformSplineDemo() {
    this(new Param());
  }

  public NonuniformSplineDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 0, 0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setQuality(graphics);
    int _degree = param.degree;
    int _levels = param.refine;
    Tensor control = getGeodesicControlPoints();
    if (1 < control.length()) {
      Tensor _effective = control;
      // ---
      int[] array = Ordering.INCREASING.of(_effective.get(Tensor.ALL, 0));
      Tensor x = Tensor.of(Arrays.stream(array).mapToObj(i -> _effective.get(i, 0)));
      Tensor y = Tensor.of(Arrays.stream(array).mapToObj(i -> _effective.get(i, 1)));
      ScalarTensorFunction scalarTensorFunction = //
          GeodesicBSplineFunction.of(RGroup.INSTANCE, _degree, x, y);
      Clip clip = Clips.interval(x.Get(0), Last.of(x));
      Tensor domain = Subdivide.increasing(clip, 4 << _levels);
      Tensor values = domain.map(scalarTensorFunction);
      Curvature2DRender.of(Transpose.of(Tensors.of(domain, values)), false).render(geometricLayer, graphics);
    }
    LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
    leversRender.renderIndexP();
  }

  static void main() {
    launch();
  }
}
