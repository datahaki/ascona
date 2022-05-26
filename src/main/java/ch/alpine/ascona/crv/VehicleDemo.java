// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

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
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
public class VehicleDemo extends ControlPointsDemo {
  BufferedImage bufferedImage = null;

  public VehicleDemo() throws IOException {
    super(true, ManifoldDisplays.SE2_ONLY);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    bufferedImage = ImageIO.read(HomeDirectory.file("vehicle.png"));
    System.out.println(bufferedImage);
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
      new ImageRenderNew(bufferedImage, //
          CoordinateBoundingBox.of(Stream.of(Clips.interval(-0.4, 1), Clips.interval(-0.35, 0.35))) //
      ).render(geometricLayer, graphics);
    }
    geometricLayer.popMatrix();
  }

  public static void main(String[] args) throws IOException {
    new VehicleDemo().setVisible(1000, 600);
  }
}
