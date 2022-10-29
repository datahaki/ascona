// code by jph
package ch.alpine.ascona.misc;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascona.util.win.AbstractBaseTest;
import ch.alpine.ascona.util.win.AbstractDemo;

class MoreTest {
  static Stream<Arguments> objectStream() {
    return List.of( //
        new BiinvariantMeanDemo(), //
        new S2ExpDemo()).stream().map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("objectStream")
  void test(Object object) {
    new AbstractBaseTest((AbstractDemo) object) {
      // ---
    };
  }
}
