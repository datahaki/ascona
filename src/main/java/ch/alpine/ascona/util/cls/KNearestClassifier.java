// code by jph
package ch.alpine.ascona.util.cls;

import java.util.Map;
import java.util.Map.Entry;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.red.Tally;

public class KNearestClassifier extends Classifier {
  private final int k;

  /** @param labels vector
   * @param k */
  public KNearestClassifier(Tensor labels, int k) {
    super(Primitives.toIntArray(labels));
    this.k = k;
  }

  @Override // from Classification
  public ClassificationResult result(Tensor weights) {
    Integers.requireEquals(weights.length(), labels.length);
    // ---
    // TODO ASCONA ALG this is not finished yet!
    Map<Tensor, Long> map = Tally.of(Ordering.INCREASING.stream(weights) //
        .limit(k) //
        .map(i -> labels[i]) //
        .map(RealScalar::of));
    Scalar lab = null;
    int cmp = 0;
    for (Entry<Tensor, Long> entry : map.entrySet())
      if (cmp < entry.getValue())
        lab = (Scalar) entry.getKey();
    return new ClassificationResult(lab.number().intValue(), RealScalar.of(0.5));
  }
}
