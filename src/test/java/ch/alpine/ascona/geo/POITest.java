// code by jph
package ch.alpine.ascona.geo;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class POITest {
  @Test
  void test() {
    LocalDate localDate = LocalDate.of(2025, 02, 21);
    DayOfWeek dayOfWeek = localDate.getDayOfWeek();
    IO.println(dayOfWeek);
  }
}
