// code by jph
package ch.alpine.ascona.usr;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import ch.alpine.bridge.fig.Plot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.sophus.math.var.VariogramFunctions;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.sca.Clips;

public enum VariogramFunctionsShow {
  ;
  public static void main(String[] args) throws IOException {
    File folder = HomeDirectory.Pictures("Variograms");
    folder.mkdir();
    Scalar[] params = { RealScalar.ZERO, RealScalar.of(0.1), RationalScalar.HALF, RealScalar.ONE, RealScalar.TWO };
    for (VariogramFunctions variograms : VariogramFunctions.values()) {
      Show show = new Show();
      show.setPlotLabel(variograms.toString());
      for (Scalar param : params)
        try {
          show.add(Plot.of(variograms.of(param), Clips.positive(2)));
        } catch (Exception exception) {
          System.out.println(variograms);
        }
      File file = new File(folder, variograms + ".png");
      show.export(file, new Dimension(500, 300));
    }
  }
}
