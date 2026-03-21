// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

import ch.alpine.ascona.crv.CurvatureParam;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
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
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.itp.BezierFunction;

/** Bezier function with extrapolation */
class BezierFunctionDemo extends PointSequenceDemo {
  @ReflectionMarker
  static class Param {
    public final CurvatureParam cp = new CurvatureParam();
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
  protected int initialCount() {
    return 3;
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
            new PathRender(ColorStroke.SECONDARY_CURVE, render, false).render(geometricLayer, graphics);
          }
        }
        Tensor refined = domain.maps(new BezierFunction(geodesicSpace, sequence));
        Tensor euclidXY = manifoldDisplay.point2xy().slash(refined);
        if (manifoldDisplay.isXYeuclid())
          Curvature2DRender.of(euclidXY, false).render(geometricLayer, graphics);
        new PathRender(ColorStroke.CURVE, euclidXY, false).render(geometricLayer, graphics);
        if (levels < 5)
          manifoldDisplay.showPoints(ColorPair.INTERMEDIATE, RealScalar.ONE, refined).render(geometricLayer, graphics);
        param.cp.spawnXY(manifoldDisplay, euclidXY, new Rectangle(0, 0, 400, 300)) //
            .render(geometricLayer, graphics);
      } catch (Exception e) {
        System.err.println("unstable");
      }
  }

  static void main() {
    new BezierFunctionDemo().runStandalone();
  }
}
