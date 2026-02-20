// code by jph
package ch.alpine.ascona.gbc.it;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.lev.LeversHud;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.dv.AffineCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophis.math.api.Genesis;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.jet.AppendOne;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.mat.pi.LeastSquares;
import ch.alpine.tensor.nrm.Vector2Norm;

public class AffineCoordinateDemo extends ControlPointsDemo {
  public AffineCoordinateDemo() {
    super(new AsconaParam(true, ManifoldDisplays.R2_ONLY));
    // ---
    Tensor sequence = Tensor.of(CirclePoints.of(7).multiply(RealScalar.of(2)).stream().map(PadRight.zeros(3)));
    setControlPointsSe2(sequence);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    // ---
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    Tensor levers = Tensor.of(sequence.stream().map(Vector2Norm.NORMALIZE));
    {
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, sequence, Array.zeros(2), geometricLayer, graphics);
      leversRender.renderSurfaceP();
    }
    {
      graphics.setColor(Color.LIGHT_GRAY);
      graphics.draw(geometricLayer.toPath2D(CirclePoints.of(31), true));
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, levers, Array.zeros(2), geometricLayer, graphics);
      leversRender.renderSequence();
      if (2 < sequence.length()) {
        // ---
        Genesis genesis = ThreePointCoordinate.of(ThreePointScalings.MEAN_VALUE);
        Tensor weights = genesis.origin(levers);
        leversRender.renderWeights(weights);
      }
    }
    geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(5, 0)));
    {
      graphics.setColor(Color.LIGHT_GRAY);
      graphics.draw(geometricLayer.toPath2D(CirclePoints.of(31), true));
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, levers, Array.zeros(2), geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderInfluenceX(LeversHud.COLOR_DATA_GRADIENT);
      if (2 < sequence.length()) {
        Genesis genesis = AffineCoordinate.INSTANCE;
        Tensor weights = genesis.origin(levers);
        leversRender.renderWeights(weights);
        Tensor lhs = Tensor.of(levers.stream().map(AppendOne.FUNCTION));
        Tensor rhs = weights;
        Tensor sol = LeastSquares.of(lhs, rhs);
        // System.out.println(sol.map(Chop._12));
        Tensor dir = sol.extract(0, 2);
        graphics.setColor(Color.RED);
        graphics.draw(geometricLayer.toLine2D(dir));
      }
    }
    geometricLayer.popMatrix();
  }

  static void main() {
    new AffineCoordinateDemo().runStandalone();
  }
}
