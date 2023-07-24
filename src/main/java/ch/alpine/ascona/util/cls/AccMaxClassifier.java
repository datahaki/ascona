// code by jph
package ch.alpine.ascona.util.cls;

import java.util.stream.IntStream;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.ext.ArgMax;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.red.Total;
import ch.alpine.tensor.sca.Clips;

public class AccMaxClassifier extends Classifier {
  /** @param labels vector */
  public AccMaxClassifier(Tensor labels) {
    super(Primitives.toIntArray(labels));
  }

  @Override // from Classification
  public ClassificationResult result(Tensor weights) {
    Tensor arguments = Array.zeros(size);
    IntStream.range(0, labels.length) //
        .forEach(index -> arguments.set(weights.get(index)::add, labels[index]));
    int label = ArgMax.of(arguments);
    Scalar confidence = //
        Clips.unit().apply(RealScalar.TWO.subtract(Total.ofVector(arguments).divide(arguments.Get(label))));
    return new ClassificationResult(label, confidence);
  }
}
