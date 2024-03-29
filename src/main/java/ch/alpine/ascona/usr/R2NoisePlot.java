// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.bridge.fig.MatrixPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.sophus.math.noise.NativeContinuousNoise;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.UnitStep;

/* package */ enum R2NoisePlot {
  ;
  private static final NativeContinuousNoise NOISE = SimplexContinuousNoise.FUNCTION;
  private static final int RES = 512;
  private static final Tensor RE = Subdivide.of(0, 5, RES - 1);
  private static final Tensor IM = Subdivide.of(0, 5, RES - 1);
  @SuppressWarnings("unused")
  private static final Clip CLIP = Clips.unit();

  private static Scalar function(int x, int y) {
    return UnitStep.FUNCTION.apply(DoubleScalar.of(NOISE.at( //
        RE.Get(x).number().doubleValue(), //
        IM.Get(y).number().doubleValue())).subtract(RealScalar.of(0.3)));
  }

  public static void main(String[] args) {
    Tensor matrix = Tensors.matrix(R2NoisePlot::function, RES, RES);
    Show show = new Show();
    show.add(MatrixPlot.of(matrix));
    ShowDialog.of(show);
  }
}
