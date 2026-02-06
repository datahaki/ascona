package ch.alpine.ascona.dat;

import org.junit.jupiter.api.Test;

class GokartPosTest {
  @Test
  void test() {
    GokartPos gokartPos = new GokartPos();
    IO.println(gokartPos.list().size());
    String string = gokartPos.list().get(2);
  gokartPos.getData(string);
  }
}
