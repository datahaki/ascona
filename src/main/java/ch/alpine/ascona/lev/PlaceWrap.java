// code by jph
package ch.alpine.ascona.lev;

import java.util.Optional;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

public class PlaceWrap {
  private final Tensor controlPoints;

  public PlaceWrap(Tensor controlPoints) {
    this.controlPoints = controlPoints;
  }

  public Optional<Tensor> getOrigin() {
    return 0 < controlPoints.length() //
        ? Optional.of(controlPoints.get(0))
        : Optional.empty();
  }

  public Tensor getSequence() {
    return 1 < controlPoints.length() //
        ? controlPoints.extract(1, controlPoints.length())
        : Tensors.empty();
  }
}
