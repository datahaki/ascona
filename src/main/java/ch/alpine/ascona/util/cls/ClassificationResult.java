// code by jph
package ch.alpine.ascona.util.cls;

import java.io.Serializable;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.sca.Clips;

public record ClassificationResult(int label, Scalar confidence) implements Serializable {
  /** @param label
   * @param confidence inside [0, 1] */
  public ClassificationResult(int label, Scalar confidence) {
    this.label = label;
    this.confidence = Clips.unit().requireInside(confidence);
  }

  /** @return */
  @Override
  public int label() {
    return label;
  }

  /** @return scalar in the interval [0, 1] */
  @Override
  public Scalar confidence() {
    return confidence;
  }
}
