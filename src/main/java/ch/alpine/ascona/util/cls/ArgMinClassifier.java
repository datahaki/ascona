// code by jph
package ch.alpine.ascona.util.cls;

import java.util.Optional;
import java.util.stream.IntStream;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.ArgMin;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Clips;

public class ArgMinClassifier extends Classifier {
  /** @param labels vector */
  public ArgMinClassifier(Tensor labels) {
    super(Primitives.toIntArray(labels));
  }

  @Override // from Classification
  public ClassificationResult result(Tensor weights) {
    Integers.requireEquals(weights.length(), labels.length);
    // ---
    int index = ArgMin.of(weights);
    int label = labels[index];
    Optional<Scalar> optional = IntStream.range(0, labels.length) //
        .filter(i -> label != labels[i]) //
        .mapToObj(weights::Get) //
        .reduce(Min::of);
    Scalar confidence = optional.isPresent() //
        ? Clips.unit().apply(RealScalar.ONE.subtract(weights.Get(index).divide(optional.orElseThrow())))
        : RealScalar.ONE;
    return new ClassificationResult(label, confidence);
  }
}
