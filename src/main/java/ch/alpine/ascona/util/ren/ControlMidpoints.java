// code by jph
package ch.alpine.ascona.util.ren;

import java.io.Serializable;
import java.util.Iterator;

import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Last;

/** creates sequence of end points and midpoints
 * 
 * {1, 2, 3} -> {1, 3/2, 5/2, 3} */
/* package */ record ControlMidpoints(GeodesicSpace geodesicSpace) implements CurveSubdivision, Serializable {
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
