// code by jph
package ch.alpine.ascona.util.dis;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.hn.HnWeierstrassCoordinate;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.pow.Power;

/* package */ enum H1Background implements RenderInterface {
  INSTANCE;

  private static final Color BORDER = new Color(192, 192, 192, 128);
  private static final Tensor H1_DOMAIN = Subdivide.of(-2.0, 2.0, 20).map(Power.function(3));

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor points = //
        Tensor.of(H1_DOMAIN.map(Tensors::of).stream().map(HnWeierstrassCoordinate::toPoint));
    // ---
    graphics.setColor(BORDER);
    graphics.draw(geometricLayer.toPath2D(points));
  }
}
