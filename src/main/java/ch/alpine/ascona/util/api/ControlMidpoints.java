// code by jph
package ch.alpine.ascona.util.api;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Last;

/** creates sequence of end points and midpoints
 * 
 * {1, 2, 3} -> {1, 3/2, 5/2, 3} */
/* package */ class ControlMidpoints implements CurveSubdivision, Serializable {
  /** @param geodesicSpace
   * @return */
  public static CurveSubdivision of(GeodesicSpace geodesicSpace) {
    return new ControlMidpoints(Objects.requireNonNull(geodesicSpace));
  }

  // ---
  private final GeodesicSpace geodesicSpace;

  private ControlMidpoints(GeodesicSpace midpointInterface) {
    this.geodesicSpace = midpointInterface;
  }

  @Override // from CurveSubdivision
  public Tensor cyclic(Tensor tensor) {
    throw new UnsupportedOperationException();
  }

  @Override // from CurveSubdivision
  public Tensor string(Tensor tensor) {
    if (Tensors.isEmpty(tensor))
      return Tensors.empty();
    Tensor result = Tensors.reserve(tensor.length() + 1);
    Iterator<Tensor> iterator = tensor.iterator();
    Tensor prev = iterator.next();
    result.append(prev);
    while (iterator.hasNext())
      result.append(geodesicSpace.midpoint(prev, prev = iterator.next()));
    result.append(Last.of(tensor));
    return result;
  }
}
