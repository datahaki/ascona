package ch.alpine.ascona.usr;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.alpine.bridge.fig.Show;

class DuneNoiseDemoTest {
  @Test
  void test(@TempDir File folder) throws IOException {
    Show show = DuneNoiseDemo.show();
    File file = new File(folder, "image.png");
    show.export(file, new Dimension(400, 200));
  }
}
