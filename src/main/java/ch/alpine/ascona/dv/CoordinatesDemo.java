// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;

import ch.alpine.ascona.lev.AbstractHoverDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public class CoordinatesDemo extends AbstractHoverDemo {
  public CoordinatesDemo() {
    super(10);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics, LeversRender leversRender) {
    Tensor weights = operator(leversRender.getSequence()).sunder(leversRender.getOrigin());
    leversRender.renderLevers(logWeighting().equals(LogWeightings.DISTANCES) //
        ? weights.negate()
        : weights);
    // ---
    leversRender.renderWeights(weights);
    leversRender.renderSequence();
    leversRender.renderOrigin();
  }

  public static void main(String[] args) {
    launch();
  }
}
