// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.BSplineLimitMatrix;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.sca.Chop;

/** functionality attempts to compute virtual control points
 * that define a cubic subdivision curve that interpolates the
 * original control points */
public class InterpolationDemo extends ControlPointsDemo {
  public InterpolationDemo() {
    super(new AsconaParam(true, ManifoldDisplays.homogeneousSpaces()));
    // ---
    addButtonDubins();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
    Tensor sequence = getGeodesicControlPoints();
    RenderQuality.setQuality(graphics);
    if (0 < sequence.length()) {
      Tensor matrix = BSplineLimitMatrix.string(sequence.length(), 3);
      Tensor invers = Inverse.of(matrix);
      try {
        Tensor tensor = Tensor.of(invers.stream() //
            .map(weights -> biinvariantMean.mean(sequence, weights)));
        CurveSubdivision curveSubdivision = new BSpline3CurveSubdivision(manifoldDisplay.geodesicSpace());
        Tensor refine = Nest.of(curveSubdivision::string, tensor, 5);
        new PathRender(Color.BLUE).setCurve(refine, false).render(geometricLayer, graphics);
      } catch (Exception exception) {
        System.err.println("something went wrong");
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
