// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.stream.Stream;

import ch.alpine.ascona.misc.VehicleStatic;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.ImageRenderNew;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
public class VehicleDemo extends ControlPointsDemo {
  public VehicleDemo() throws IOException {
    super(true, ManifoldDisplays.SE2_ONLY);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    // graphics.drawImage(bufferedImage, 0, 0, null);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    geometricLayer.pushMatrix(GfxMatrix.of(mouse));
    {
      new ImageRenderNew(VehicleStatic.INSTANCE.bufferedImage_c(), //
          CoordinateBoundingBox.of(Stream.of(Clips.interval(-0.4, 1), Clips.interval(-0.35, 0.35))) //
      ).render(geometricLayer, graphics);
    }
    geometricLayer.popMatrix();
  }

  public static void main(String[] args) throws IOException {
    new VehicleDemo().setVisible(1000, 600);
  }
}
