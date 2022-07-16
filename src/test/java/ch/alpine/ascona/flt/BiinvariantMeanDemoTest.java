// code by jph
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascona.util.win.AbstractDemoHelper.Holder;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.FieldsAssignment;
import ch.alpine.bridge.ref.util.ObjectProperties;
import ch.alpine.bridge.ref.util.RandomFieldsAssignment;

class BiinvariantMeanDemoTest {
  static Stream<Arguments> objectStream() {
    BiinvariantMeanDemo biinvariantMeanDemo = new BiinvariantMeanDemo();
    Holder holder = new Holder(biinvariantMeanDemo.objects());
    FieldsAssignment fieldsAssignment = RandomFieldsAssignment.of(holder);
    return fieldsAssignment.randomize(10).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("objectStream")
  void test(Object object) {
    BiinvariantMeanDemo biinvariantMeanDemo = new BiinvariantMeanDemo();
    Holder holder = new Holder(biinvariantMeanDemo.objects());
    ObjectProperties.part(holder, ObjectProperties.join(object));
    biinvariantMeanDemo.fieldsEditor(0).notifyUniversalListeners();
    GeometricLayer geometricLayer = new GeometricLayer(biinvariantMeanDemo.timerFrame.geometricComponent.getModel2Pixel());
    BufferedImage bufferedImage = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    biinvariantMeanDemo.render(geometricLayer, graphics);
    graphics.dispose();
  }
}
