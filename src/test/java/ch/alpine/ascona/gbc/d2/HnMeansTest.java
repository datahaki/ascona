// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.Random;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.h.HWeierstrassCoordinate;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.nrm.NormalizeTotal;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;

class HnMeansTest {
  @Test
  void testSimple() {
    RandomGenerator randomGenerator = new Random(3);
    Distribution distribution = NormalDistribution.standard();
    for (HnMeans hnMeans : HnMeans.values()) {
      BiinvariantMean biinvariantMean = hnMeans.get();
      for (int d = 1; d < 5; ++d) {
        final int fd = d;
        Tensor sequence = Array.of(_ -> new HWeierstrassCoordinate(RandomVariate.of(distribution, randomGenerator, fd)).toPoint(), 10);
        Tensor weights = NormalizeTotal.FUNCTION.apply(RandomVariate.of(UniformDistribution.unit(), randomGenerator, 10));
        biinvariantMean.mean(sequence, weights);
      }
    }
  }
}
