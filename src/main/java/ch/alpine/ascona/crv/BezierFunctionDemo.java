// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.BezierFunction;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.Chop;

/** Bezier function with extrapolation */
@ReflectionMarker
public class BezierFunctionDemo extends AbstractCurvatureDemo {
  @FieldPreferredWidth(200)
  @FieldSlider
  @FieldInteger
  @FieldClip(min = "0", max = "10")
  public Scalar refine = RealScalar.of(6);
  public Boolean extrap = false;

  public BezierFunctionDemo() {
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    addButtonDubins();
    {
      Tensor tensor = Tensors.fromString("{{0, 1, 0}, {1, 0, 0}}");
      setControlPointsSe2(tensor);
    }
    setGeodesicDisplay(Se2Display.INSTANCE);
    timerFrame.geometricComponent.addRenderInterfaceBackground(AxesRender.INSTANCE);
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setQuality(graphics);
    // ---
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    int n = sequence.length();
    if (0 == n)
      return Tensors.empty();
    int levels = refine.number().intValue();
    Tensor domain = n <= 1 //
        ? Tensors.vector(0)
        : Subdivide.of(0.0, extrap //
            ? n / (double) (n - 1)
            : 1.0, 1 << levels);
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    if (geodesicSpace instanceof HomogeneousSpace homogeneousSpace) {
      BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
      if (Objects.nonNull(biinvariantMean)) {
        Tensor refined = domain.map(BezierFunction.of(biinvariantMean, sequence));
        Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::toPoint));
        new PathRender(Color.RED, 1.25f).setCurve(render, false).render(geometricLayer, graphics);
      }
    }
    Tensor refined = domain.map(BezierFunction.of(geodesicSpace, sequence));
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::toPoint));
    Curvature2DRender.of(render, false, geometricLayer, graphics);
    if (levels < 5)
      renderPoints(manifoldDisplay, refined, geometricLayer, graphics);
    return refined;
  }

  public static void main(String[] args) {
    LookAndFeels.DARK.updateUI();
    new BezierFunctionDemo().setVisible(1000, 600);
  }
}
