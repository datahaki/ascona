// code by jph
package ch.alpine.ascona.util.win;

import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.AbstractManifoldDisplayDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.bridge.gfx.GeometricLayer;

public enum AbstractDemoHelper {
  ;
  /** off-screen test
   * 
   * @param abstractDemo
   * @throws IllegalAccessException
   * @throws IllegalArgumentException */
  public static void offscreen(AbstractDemo abstractDemo) {
    // try {
    // Class<? extends AbstractDemo> class2 = abstractDemo.getClass();
    // System.out.println(class2);
    // for (Field field : class2.getFields()) {
    // FieldTest annotation = field.getAnnotation(FieldTest.class);
    // if (Objects.nonNull(annotation)) {
    // System.out.println(field.getName());
    // System.out.println(annotation);
    // Class<?> class1 = field.getType();
    // System.out.println(class1);
    // System.out.println(class1.equals(SpinnerLabel.class));
    // try {
    // Object sl = field.get(abstractDemo);
    // System.out.println(sl.getClass());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // } catch (Exception exception) {
    // throw new RuntimeException(exception);
    // }
    // abstractDemo.timerFrame.geometricComponent.getMouseSe2CState()
    GeometricLayer geometricLayer = new GeometricLayer( //
        abstractDemo.timerFrame.geometricComponent.getModel2Pixel() //
    );
    BufferedImage bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
    abstractDemo.render(geometricLayer, bufferedImage.createGraphics());
    boolean success = true;
    if (abstractDemo instanceof AbstractManifoldDisplayDemo) {
      AbstractManifoldDisplayDemo geodesicDisplayDemo = (AbstractManifoldDisplayDemo) abstractDemo;
      for (ManifoldDisplay manifoldDisplay : geodesicDisplayDemo.getManifoldDisplays())
        try {
          geodesicDisplayDemo.setManifoldDisplay(manifoldDisplay);
          geodesicDisplayDemo.render(geometricLayer, bufferedImage.createGraphics());
        } catch (Exception exception) {
          System.err.println(manifoldDisplay);
          success = false;
        }
    }
    if (!success)
      throw new RuntimeException();
  }
}