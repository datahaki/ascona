// code by jph
package ch.alpine.ascona.util.cls;

import java.util.Optional;
import java.util.stream.IntStream;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.ArgMax;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.sca.Clips;

public class ArgMaxClassifier extends Classifier {
  /** @param labels vector */
  public ArgMaxClassifier(Tensor labels) {
    super(Primitives.toIntArray(labels));
  }

  @Override // from Classification
  public ClassificationResult result(Tensor weights) {
    int length = Integers.requireEquals(weights.length(), labels.length);
    int index = ArgMax.of(weights);
    int label = labels[index];
    Optional<Scalar> optional = IntStream.range(0, length) //
        .filter(i -> label != labels[i]) //
        .mapToObj(weights::Get) //
        .reduce(Max::of);
    // clip shouldn't be necessary but exists for negative
    // weights and to correct numerical imprecision
    Scalar confidence = optional.isPresent() //
        ? Clips.unit().apply(RealScalar.ONE.subtract(optional.orElseThrow().divide(weights.Get(index))))
        : RealScalar.ONE;
    return new ClassificationResult(label, confidence);
  }
}
