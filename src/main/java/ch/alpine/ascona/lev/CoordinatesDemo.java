// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public class CoordinatesDemo extends AbstractHoverDemo {
  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics, LeversRender leversRender) {
    Tensor weights = operator(leversRender.getSequence()).apply(leversRender.getOrigin());
    leversRender.renderLevers(logWeighting().equals(LogWeightings.DISTANCES) //
        ? weights.negate()
        : weights);
    // ---
    leversRender.renderWeights(weights);
    leversRender.renderSequence();
    leversRender.renderOrigin();
  }

  public static void main(String[] args) {
    new CoordinatesDemo().setVisible(1200, 900);
  }
}
