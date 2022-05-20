// code by jph, gjoel
package ch.alpine.ascona.nd;

import java.util.Collection;
import java.util.Queue;
import java.util.function.Function;

import ch.alpine.bridge.util.BoundedPriorityQueue;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidComparators;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.NdCenters;
import ch.alpine.tensor.opt.nd.NdCollectNearest;
import ch.alpine.tensor.opt.nd.NdMap;
import ch.alpine.tensor.opt.nd.NdMatch;
import ch.alpine.tensor.opt.nd.NdTreeMap;

public class ClothoidNdMap<T> {
  private final int FACTOR = 3;
  private final NdMap<T> ndMap;
  private final Function<T, Tensor> se2Projection;

  public ClothoidNdMap(CoordinateBoundingBox coordinateBoundingBox, Function<T, Tensor> se2Projection) {
    Integers.requireEquals(coordinateBoundingBox.dimensions(), 2);
    ndMap = NdTreeMap.of(coordinateBoundingBox);
    this.se2Projection = se2Projection;
  }

  public void insert(T value) {
    ndMap.insert(se2Projection.apply(value).extract(0, 2), value);
  }

  public Collection<Clothoid> cl_nearFrom(ClothoidBuilder clothoidBuilder, T value, int limit) {
    Tensor origin = se2Projection.apply(value);
    Collection<NdMatch<T>> collection = //
        NdCollectNearest.of(ndMap, NdCenters.VECTOR_2_NORM.apply(origin.extract(0, 2)), limit * FACTOR);
    Queue<Clothoid> queue = BoundedPriorityQueue.min(limit, ClothoidComparators.LENGTH);
    for (NdMatch<T> ndMatch : collection)
      queue.add(clothoidBuilder.curve(origin, se2Projection.apply(ndMatch.value())));
    return queue;
  }

  public Collection<Clothoid> cl_nearTo(ClothoidBuilder clothoidBuilder, T value, int limit) {
    Tensor target = se2Projection.apply(value);
    Collection<NdMatch<T>> collection = //
        NdCollectNearest.of(ndMap, NdCenters.VECTOR_2_NORM.apply(target.extract(0, 2)), limit * FACTOR);
    Queue<Clothoid> queue = BoundedPriorityQueue.min(limit, ClothoidComparators.LENGTH);
    for (NdMatch<T> ndMatch : collection)
      queue.add(clothoidBuilder.curve(se2Projection.apply(ndMatch.value()), target));
    return queue;
  }

  public int size() {
    return ndMap.size();
  }
}
