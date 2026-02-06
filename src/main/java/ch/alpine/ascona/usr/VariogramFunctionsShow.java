// code by jph
package ch.alpine.ascona.usr;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.bridge.fig.Plot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.var.VariogramFunctions;

public enum VariogramFunctionsShow {
  ;
  static void main() throws IOException {
    Path folder = HomeDirectory.Pictures.resolve("Variograms");
    Files.createDirectories(folder);
    Scalar[] params = { RealScalar.ZERO, RealScalar.of(0.1), RationalScalar.HALF, RealScalar.ONE, RealScalar.TWO };
    for (VariogramFunctions variograms : VariogramFunctions.values()) {
      Show show = new Show();
      show.setPlotLabel(variograms.toString());
      for (Scalar param : params)
        try {
          show.add(Plot.of(variograms.of(param), Clips.positive(2)));
          Path file = folder.resolve(variograms + ".png");
          show.export(file, new Dimension(500, 300));
        } catch (Exception exception) {
          System.out.println(variograms);
        }
    }
  }
}
