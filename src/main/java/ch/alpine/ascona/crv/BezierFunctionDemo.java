// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascona.util.api.ControlPointsStatic;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.BezierFunction;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.Chop;

/** Bezier function with extrapolation */
public class BezierFunctionDemo extends AbstractCurvatureDemo {
  @ReflectionMarker
  public static class Param extends AbstractCurvatureParam {
    public Param() {
      super(ManifoldDisplays.ALL);
    }

    @FieldSlider
    @FieldInteger
    @FieldClip(min = "0", max = "10")
    public Scalar refine = RealScalar.of(6);
    public Boolean extrap = false;
  }

  private final Param param;

  public BezierFunctionDemo() {
    this(new Param());
  }

  public BezierFunctionDemo(Param param) {
    super(param);
    this.param = param;
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    addButtonDubins();
    {
      Tensor tensor = Tensors.fromString("{{0, 1, 0}, {1, 0, 0}}");
      setControlPointsSe2(tensor);
    }
    setManifoldDisplay(ManifoldDisplays.Se2);
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setQuality(graphics);
    // ---
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    int n = sequence.length();
    if (0 == n)
      return Tensors.empty();
    int levels = param.refine.number().intValue();
    Tensor domain = n <= 1 //
        ? Tensors.vector(0)
        : Subdivide.of(0.0, param.extrap //
            ? n / (double) (n - 1)
            : 1.0, 1 << levels);
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    if (geodesicSpace instanceof HomogeneousSpace homogeneousSpace) {
      BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
      if (Objects.nonNull(biinvariantMean)) {
        Tensor refined = domain.map(BezierFunction.of(biinvariantMean, sequence));
        Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
        new PathRender(Color.RED, 1.25f).setCurve(render, false).render(geometricLayer, graphics);
      }
    }
    Tensor refined = domain.map(new BezierFunction(geodesicSpace, sequence));
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    Curvature2DRender.of(render, false, geometricLayer, graphics);
    if (levels < 5)
      ControlPointsStatic.renderPoints(manifoldDisplay, refined, geometricLayer, graphics);
    return refined;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new BezierFunctionDemo().setVisible(1200, 600);
  }
}
