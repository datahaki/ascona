// code by jph
package ch.alpine.ascona.util.win;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.FieldsAssignment;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.bridge.ref.util.OuterFieldsAssignment;

public enum AbstractDemoHelper {
  ;
  public static void offscreen(AbstractDemo abstractDemo) {
    Object object = abstractDemo.object();
    if (Objects.nonNull(object)) {
      AtomicInteger atomicInteger = new AtomicInteger();
      GeometricLayer geometricLayer = new GeometricLayer(abstractDemo.timerFrame.geometricComponent.getModel2Pixel());
      BufferedImage bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
      FieldsAssignment outerFieldsAssignment = OuterFieldsAssignment.of(object, () -> {
        try {
          abstractDemo.fieldsEditor.notifyUniversalListeners();
          abstractDemo.render(geometricLayer, bufferedImage.createGraphics());
        } catch (Exception exception) {
          exception.printStackTrace();
          System.err.println(ObjectProperties.join(object));
        }
        atomicInteger.getAndIncrement();
      });
      outerFieldsAssignment.randomize(50);
      System.out.println(abstractDemo.getClass().getSimpleName() + " " + atomicInteger.get());
    }
  }
}
