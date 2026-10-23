package ch.alpine.ascona.geo;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

@ReflectionMarker
class MapParam {
  public Tensor center = Tensors.vector(0, 0, 0);
}
