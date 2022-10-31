// code by jph
package ch.alpine.ascona.usr;

import java.io.IOException;

import ch.alpine.bridge.fig.DensityPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

public enum NoiseDemo {
  ;
  public static void main(String[] args) throws IOException {
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of(Clips.absoluteOne(), Clips.absoluteOne());
    Show show1 = new Show();
    show1.setPlotLabel("SimplexContinuousNoise[x,y]");
    show1.add(DensityPlot.of( //
        (x, y) -> DoubleScalar.of(SimplexContinuousNoise.FUNCTION.at(x.number().doubleValue(), y.number().doubleValue())), //
        cbb));
    Show show2 = new Show();
    show2.setPlotLabel("SimplexContinuousNoise[x,y,0]");
    show2.add(DensityPlot.of( //
        (x, y) -> DoubleScalar.of(SimplexContinuousNoise.FUNCTION.at(x.number().doubleValue(), y.number().doubleValue(), 0.0)), //
        cbb));
    Show show3 = new Show();
    show3.setPlotLabel("Empty");
    ShowDialog.of(show1, show2, show3);
  }
}
