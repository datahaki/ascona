// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.api.GeoPosition;
import ch.alpine.sophus.hs.s.SnManifold;
import ch.alpine.sophus.hs.s.Sphere;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.AdjacentReduce;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.red.Total;

@ReflectionMarker
public class GeoPath {
  public Tensor pathSeq = Tensors.empty();

  public Scalar distance() {
    TensorUnaryOperator tuo = GeoPosition::of;
    Tensor tensor = tuo.slash(pathSeq);
    SnManifold snManifold=  Sphere.INSTANCE;
    AdjacentReduce adjacentReduce = new AdjacentReduce((p, q) -> snManifold.bilinearForm(p).norm(snManifold.tangentSpace(p).log(q)));
    Tensor parts = adjacentReduce.apply(tensor);
    return Total.ofVector(parts).multiply(Quantity.of(6_378_137, "m"));
  }
}
