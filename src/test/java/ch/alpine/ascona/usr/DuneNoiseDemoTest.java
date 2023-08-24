package ch.alpine.ascona.usr;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.alpine.bridge.fig.DensityPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.tensor.ext.Integers;

class DuneNoiseDemoTest {
  @Test
  void test(@TempDir File folder) throws IOException {
    DensityPlot densityPlot = DuneNoiseDemo.densityPlot();
    BufferedImage bufferedImage = densityPlot.getImage();
    Integers.requireLessEquals(bufferedImage.getWidth(), 30);
    File file = new File(folder, "image.png");
    Show show = new Show();
    show.add(densityPlot);
    show.export(file, new Dimension(400, 200));
  }
}
