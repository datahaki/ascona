// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascony.dat.PlaceWrap;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

class TangentsDemo extends ControlPointsDemo {
  public TangentsDemo() {
    setControlPointsSe2(Tensors.fromString("{{-0.3, 0.0, 0}, {0.0, 0.5, 0.0}, {0.5, 0.5, 1}, {0.5, -0.4, 0}}"));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.ADDREMOVE;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor origin = optional.orElseThrow();
      Tensor sequence = placeWrap.getSequence();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay(), sequence, origin, geometricLayer, graphics);
      leversRender.renderLevers();
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderTangentsPtoX(true);
      leversRender.renderTangentsXtoP(true);
      leversRender.renderIndexP();
      leversRender.renderIndexX();
    }
  }

  static void main() {
    new TangentsDemo().runStandalone();
  }
}
