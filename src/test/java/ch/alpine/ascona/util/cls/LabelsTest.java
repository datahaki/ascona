// code by jph
package ch.alpine.ascona.util.cls;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

class LabelsTest {
  @Test
  void testSimple() {
    Distribution distribution = UniformDistribution.unit();
    for (Labels labels : Labels.values()) {
      Tensor vector = Tensors.vector(3, 1, 0, 2, 0, 1, 3);
      Classification classification = labels.apply(vector);
      classification.result(RandomVariate.of(distribution, vector.length()));
      assertThrows(Exception.class, () -> classification.result(RandomVariate.of(distribution, vector.length() - 1)));
    }
  }
}
