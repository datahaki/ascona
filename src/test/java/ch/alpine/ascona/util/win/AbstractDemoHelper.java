// code by jph
package ch.alpine.ascona.util.win;

import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.AbstractManifoldDisplayDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.gfx.GeometricLayer;

public enum AbstractDemoHelper {
  ;
  /** off-screen test
   * 
   * @param abstractDemo
   * @throws IllegalAccessException
   * @throws IllegalArgumentException */
  public static void offscreen(AbstractDemo abstractDemo) {
    GeometricLayer geometricLayer = new GeometricLayer( //
        abstractDemo.timerFrame.geometricComponent.getModel2Pixel() //
    );
    BufferedImage bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
    abstractDemo.render(geometricLayer, bufferedImage.createGraphics());
    boolean success = true;
    if (abstractDemo instanceof AbstractManifoldDisplayDemo) {
      AbstractManifoldDisplayDemo abstractManifoldDisplayDemo = (AbstractManifoldDisplayDemo) abstractDemo;
      for (ManifoldDisplays manifoldDisplays : abstractManifoldDisplayDemo.getManifoldDisplays())
        try {
          abstractManifoldDisplayDemo.setManifoldDisplay(manifoldDisplays);
          // TODO
          // abstractManifoldDisplayDemo.reportToAll();
          abstractManifoldDisplayDemo.render(geometricLayer, bufferedImage.createGraphics());
        } catch (Exception exception) {
          System.err.println(manifoldDisplays);
          success = false;
        }
    }
    if (!success)
      throw new RuntimeException();
  }
}
