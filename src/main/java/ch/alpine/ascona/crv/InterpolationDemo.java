// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.BSplineLimitMatrix;
import ch.alpine.sophus.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.red.Nest;

// TODO ASCONA ALG functionality/purpose is not clear
public class InterpolationDemo extends ControlPointsDemo {
  public InterpolationDemo() {
    super(true, ManifoldDisplays.SE2C_SE2_R2);
    // ---
    addButtonDubins();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
    Tensor sequence = getGeodesicControlPoints();
    RenderQuality.setQuality(graphics);
    if (0 < sequence.length()) {
      Tensor matrix = BSplineLimitMatrix.string(sequence.length(), 3);
      Tensor invers = Inverse.of(matrix);
      Tensor tensor = Tensor.of(invers.stream().map(weights -> biinvariantMean.mean(sequence, weights)));
      CurveSubdivision curveSubdivision = new BSpline3CurveSubdivision(manifoldDisplay.geodesicSpace());
      Tensor refine = Nest.of(curveSubdivision::string, tensor, 5);
      new PathRender(Color.BLUE).setCurve(refine, false).render(geometricLayer, graphics);
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    new InterpolationDemo().setVisible(1000, 800);
  }
}
