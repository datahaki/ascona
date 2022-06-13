// code by jph
package ch.alpine.ascona.util.win;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntervalClockTest {
  @Test
  void testHertz() {
    IntervalClock intervalClock = new IntervalClock();
    double hertz = intervalClock.hertz();
    assertTrue(100 < hertz);
  }

  @Test
  void testSeconds() {
    IntervalClock intervalClock = new IntervalClock();
    double seconds = intervalClock.seconds();
    assertTrue(seconds < 0.01);
  }
}
