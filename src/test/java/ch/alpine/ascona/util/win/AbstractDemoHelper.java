// code by jph
package ch.alpine.ascona.util.win;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.FieldsAssignment;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.bridge.ref.util.RandomFieldsAssignment;

public class AbstractDemoHelper implements Runnable {
  /** @param abstractDemo */
  public static void offscreen(AbstractDemo abstractDemo) {
    if (Objects.nonNull(abstractDemo.object())) {
      AbstractDemoHelper abstractDemoHelper = new AbstractDemoHelper(abstractDemo);
      abstractDemoHelper.randomize(50);
      String string = abstractDemo.getClass().getSimpleName();
      if (0 < abstractDemoHelper.error.get())
        System.err.println(string + " err=" + abstractDemoHelper.error.get() + " (of " + abstractDemoHelper.total.get() + ")");
      else
        System.out.println(string + " ok=" + abstractDemoHelper.total.get());
    }
  }

  private final AbstractDemo abstractDemo;
  private final Object object;
  private final AtomicInteger error = new AtomicInteger();
  private final AtomicInteger total = new AtomicInteger();
  private final GeometricLayer geometricLayer;
  private final BufferedImage bufferedImage;
  private final FieldsAssignment fieldsAssignment;

  private AbstractDemoHelper(AbstractDemo abstractDemo) {
    this.abstractDemo = abstractDemo;
    object = abstractDemo.object();
    geometricLayer = new GeometricLayer(abstractDemo.timerFrame.geometricComponent.getModel2Pixel());
    bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
    abstractDemo.render(geometricLayer, bufferedImage.createGraphics());
    fieldsAssignment = RandomFieldsAssignment.of(object, this);
  }

  private void randomize(int count) {
    fieldsAssignment.randomize(count);
  }

  @Override
  public void run() {
    try {
      abstractDemo.fieldsEditor.notifyUniversalListeners();
      abstractDemo.render(geometricLayer, bufferedImage.createGraphics());
    } catch (Exception exception) {
      exception.printStackTrace();
      System.err.println(ObjectProperties.join(object));
      error.getAndIncrement();
    }
    total.getAndIncrement();
  }
}
