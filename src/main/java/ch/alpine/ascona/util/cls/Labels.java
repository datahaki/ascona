// code by jph
package ch.alpine.ascona.util.cls;

import java.util.function.Function;

import ch.alpine.tensor.Tensor;

public enum Labels implements Function<Tensor, Classification> {
  ARG_MIN(Classifier::argMin),
  NEAREST1(l -> new KNearestClassifier(l, 1)),
  NEAREST3(l -> new KNearestClassifier(l, 3)),
  NEAREST5(l -> new KNearestClassifier(l, 5)),
  ARG_MAX(Classifier::argMax),
  ACC_MAX(Classifier::accMax);

  private final Function<Tensor, Classification> function;

  Labels(Function<Tensor, Classification> function) {
    this.function = function;
  }

  @Override
  public Classification apply(Tensor vector) {
    return function.apply(vector);
  }
}
