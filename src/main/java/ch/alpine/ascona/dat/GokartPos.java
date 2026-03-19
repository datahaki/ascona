// code by jph
package ch.alpine.ascona.dat;

import java.util.List;

import ch.alpine.ascony.dat.Se2Pos;
import ch.alpine.ascony.dat.Se2PosHz;
import ch.alpine.ascony.res.ResourceMapper;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.red.Mean;

public enum GokartPos {
  INSTANCE;

  private final ResourceMapper resourceMapper = //
      ResourceMapper.of("ch/alpine/ascona/gokart/resource_index.vector");
  private final TensorUnaryOperator EXTRACT_POS = row -> row.extract(1, 4);

  public List<String> keys() {
    return resourceMapper.list();
  }

  public Se2PosHz get(String key, int limit) {
    Tensor tensor = Tensor.of(resourceMapper.importResource(key).stream().limit(limit));
    /** @return quantity with unit Hz */
    Scalar samplingRate = Quantity.of(Mean.ofVector(Differences.of(tensor.get(Tensor.ALL, 0))).reciprocal(), "Hz");
    return new Se2PosHz(new Se2Pos(EXTRACT_POS.slash(tensor)), samplingRate);
  }
}
