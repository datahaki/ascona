// code by jph
package ch.alpine.ascona.misc;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

public class ImageRenderDemo extends AbstractDemo {
  private static final CoordinateBoundingBox COORDINATE_BOUNDING_BOX = //
      CoordinateBoundingBox.of(Clips.interval(-0.4, 1), Clips.interval(-0.35, 0.35));

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    geometricLayer.pushMatrix(GfxMatrix.of(mouse));
    new ImageRender( //
        VehicleStatic.INSTANCE.bufferedImage_c(), //
        COORDINATE_BOUNDING_BOX).render(geometricLayer, graphics);
    geometricLayer.popMatrix();
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new ImageRenderDemo().setVisible(1000, 600);
  }
}
