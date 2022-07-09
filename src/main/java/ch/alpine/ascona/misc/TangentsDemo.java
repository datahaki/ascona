// code by jph
package ch.alpine.ascona.misc;

import java.awt.Graphics2D;
import java.util.Optional;

import ch.alpine.ascona.lev.PlaceWrap;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

public class TangentsDemo extends ControlPointsDemo {
  public TangentsDemo() {
    super(new AsconaParam(true, ManifoldDisplays.manifolds()));
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{-0.3, 0.0, 0}, {0.0, 0.5, 0.0}, {0.5, 0.5, 1}, {0.5, -0.4, 0}}"));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor origin = optional.orElseThrow();
      Tensor sequence = placeWrap.getSequence();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay(), sequence, origin, geometricLayer, graphics);
      leversRender.renderLevers();
      leversRender.renderOrigin();
      leversRender.renderSequence();
      leversRender.renderTangentsPtoX(true);
      leversRender.renderTangentsXtoP(true);
      leversRender.renderIndexP();
      leversRender.renderIndexX();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
