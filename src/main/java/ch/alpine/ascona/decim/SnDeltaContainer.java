// code by jph
package ch.alpine.ascona.decim;

import java.util.List;
import java.util.stream.IntStream;

import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Spectrogram;
import ch.alpine.sophus.hs.HsDifferences;
import ch.alpine.sophus.hs.s.SnManifold;
import ch.alpine.sophus.hs.s.TSnMemberQ;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradients;

public class SnDeltaContainer {
  final Tensor sequence;
  final Tensor differences;
  private final List<Tensor> endos;
  private final Tensor t0_deltas;
  final Show show1 = new Show();
  final Show[] shows = { new Show(), new Show() };
  // private final Tensor[] spectrogram = new Tensor[2];
  // final BufferedImage[] bufferedImage = new BufferedImage[2];

  public SnDeltaContainer(Tensor sequence, ScalarUnaryOperator window) {
    this.sequence = sequence;
    endos = SnTransportChain.endos(sequence);
    differences = HsDifferences.of(SnManifold.INSTANCE).apply(sequence);
    TSnMemberQ tSnMemberQ = new TSnMemberQ(sequence.get(0));
    t0_deltas = Tensor.of(IntStream.range(0, differences.length()).mapToObj( //
        index -> tSnMemberQ.require(endos.get(index).dot(differences.get(index, 1)))));
    // ---
    Tensor domain = Range.of(0, t0_deltas.length());
    for (int d = 1; d < 3; ++d) {
      Tensor values = t0_deltas.get(Tensor.ALL, d);
      // spectrogram[d - 1] = Spectrogram.vector(values, window, ColorDataGradients.VISIBLE_SPECTRUM);
      // bufferedImage[d - 1] = ImageFormat.of(spectrogram[d - 1]);
      shows[d - 1].add(Spectrogram.of(values, RealScalar.ONE, window, ColorDataGradients.VISIBLE_SPECTRUM));
      show1.add(ListLinePlot.of(domain, values));
    }
  }
}
