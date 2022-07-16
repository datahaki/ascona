// code by jph
package ch.alpine.ascona.util.win;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.lang.ShortStackTrace;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsAssignment;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.bridge.ref.util.RandomFieldsAssignment;

public class AbstractDemoHelper {
  private static final ShortStackTrace SHORT_STACK_TRACE = new ShortStackTrace("ch.alpine.");
  private static final int MAX = 20;

  /** @param abstractDemo non-null */
  public static AbstractDemoHelper offscreen(AbstractDemo abstractDemo) {
    AbstractDemoHelper abstractDemoHelper = new AbstractDemoHelper(abstractDemo);
    abstractDemoHelper.randomize(MAX);
    String string = abstractDemo.getClass().getSimpleName();
    if (0 < abstractDemoHelper.error.get())
      System.err.println(string + " " + abstractDemoHelper.error.get() + " Errors");
    return abstractDemoHelper;
  }

  @ReflectionMarker
  public static class Holder {
    public final Object[] objects;

    @SafeVarargs
    public Holder(Object... objects) {
      this.objects = objects;
    }

    @Override
    public String toString() {
      return ObjectProperties.join(this);
    }
  }

  private final AbstractDemo abstractDemo;
  private final Holder holder;
  private final AtomicInteger error = new AtomicInteger();
  private final AtomicInteger total = new AtomicInteger();
  private final GeometricLayer geometricLayer;
  private final BufferedImage bufferedImage;
  private final FieldsAssignment fieldsAssignment;

  private AbstractDemoHelper(AbstractDemo abstractDemo) {
    this.abstractDemo = abstractDemo;
    holder = new Holder(abstractDemo.objects());
    geometricLayer = new GeometricLayer(abstractDemo.timerFrame.geometricComponent.getModel2Pixel());
    bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
    try_wrap(this::render);
    fieldsAssignment = RandomFieldsAssignment.of(holder);
  }

  private void randomize(int count) {
    fieldsAssignment.randomize(count).forEach(this::set_render);
  }

  private void set_render(Object ignore) {
    Objects.requireNonNull(ignore);
    try_wrap(() -> {
      for (int index = 0; index < holder.objects.length; ++index)
        abstractDemo.fieldsEditor(index).notifyUniversalListeners();
      render();
    });
  }

  private void render() {
    Graphics2D graphics = bufferedImage.createGraphics();
    abstractDemo.render(geometricLayer, graphics);
    graphics.dispose();
  }

  /** @param runnable */
  private void try_wrap(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception exception) {
      System.err.println("Error in " + abstractDemo.getClass().getSimpleName() + ":");
      System.err.println(ObjectProperties.join(holder));
      SHORT_STACK_TRACE.print(exception);
      error.getAndIncrement();
    }
    total.getAndIncrement();
  }

  public int errors() {
    return error.get();
  }
}
