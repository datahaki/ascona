// code by jph
package ch.alpine.ascona.util.win;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascona.util.win.AbstractDemoHelper.Holder;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.RandomFieldsAssignment;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractBaseTest {
  final AbstractDemo abstractDemo;
  final Holder holder;

  public AbstractBaseTest(AbstractDemo abstractDemo) {
    this.abstractDemo = abstractDemo;
    holder = new Holder(abstractDemo.objects());
  }

  Stream<Arguments> objectStream() {
    return RandomFieldsAssignment.of(holder).randomize(10).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("objectStream")
  void test(Object object) {
    abstractDemo.fieldsEditor(0).notifyUniversalListeners();
    GeometricLayer geometricLayer = new GeometricLayer(abstractDemo.timerFrame.geometricComponent.getModel2Pixel());
    BufferedImage bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    abstractDemo.render(geometricLayer, graphics);
    graphics.dispose();
  }
}
