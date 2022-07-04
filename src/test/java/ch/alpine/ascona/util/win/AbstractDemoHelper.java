// code by jph
package ch.alpine.ascona.util.win;

import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.Objects;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.FieldOuterProduct;
import ch.alpine.bridge.ref.util.ObjectProperties;

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
    if (abstractDemo instanceof ControlPointsDemo) {
      ControlPointsDemo controlPointsDemo = (ControlPointsDemo) abstractDemo;
      AsconaParam asconaParam = controlPointsDemo.asconaParam();
      Constructor<?> constructor = null;
      try {
        constructor = abstractDemo.getClass().getConstructor(asconaParam.getClass());
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      if (Objects.nonNull(constructor)) {
        Constructor<?> fi_constructor = constructor;
        FieldOuterProduct.forEach(asconaParam, a -> {
          try {
            Object newInstance = fi_constructor.newInstance(a);
            ControlPointsDemo cpd = (ControlPointsDemo) newInstance;
            cpd.render(geometricLayer, bufferedImage.createGraphics());
          } catch (Exception exception) {
            System.out.println("settings bad:");
            System.err.println(ObjectProperties.join(a));
            throw new RuntimeException(exception);
          }
        });
      }
    }
  }
}
