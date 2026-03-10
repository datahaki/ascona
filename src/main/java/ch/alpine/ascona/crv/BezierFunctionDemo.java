// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.BaseCurvatureParam;
import ch.alpine.ascony.ren.ControlPointsStatic;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.BezierCurve;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.itp.BezierFunction;

/** Bezier function with extrapolation */
class BezierFunctionDemo extends PointSequenceDemo {
  @ReflectionMarker
  static class Param extends BaseCurvatureParam {
    @FieldSlider
    @FieldClip(min = "0", max = "10")
    public Integer refine = 6;
    public Boolean extrapolate = false;
  }

  private final Param param;

  public BezierFunctionDemo() {
    super(param = new Param());
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    int n = sequence.length();
    if (0 != n)
      try {
        int levels = param.refine;
        Tensor domain = n <= 1 //
            ? Tensors.vector(0)
            : Subdivide.of(0.0, param.extrapolate //
                ? n / (double) (n - 1)
                : 1.0, 1 << levels);
        GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
        if (geodesicSpace instanceof HomogeneousSpace homogeneousSpace) {
          // BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
          if (Objects.nonNull(homogeneousSpace)) {
            Tensor refined = domain.maps(BezierCurve.of(homogeneousSpace.biinvariantMean(), sequence));
            Tensor render = manifoldDisplay.point2xy().slash(refined);
            new PathRender(Color.RED, 1.25f).setCurve(render, false).render(geometricLayer, graphics);
          }
        }
        Tensor refined = domain.maps(new BezierFunction(geodesicSpace, sequence));
        Tensor render = manifoldDisplay.point2xy().slash(refined);
        Curvature2DRender.of(render, false).render(geometricLayer, graphics);
        if (levels < 5)
          ControlPointsStatic.gray(manifoldDisplay, refined).render(geometricLayer, graphics);
        param.spawn(manifoldDisplay, refined, new Rectangle(0, 0, 400, 300)) //
            .render(geometricLayer, graphics);
      } catch (Exception e) {
        System.err.println("unstable");
      }
  }

  static void main() {
    new BezierFunctionDemo().runStandalone();
  }
}
