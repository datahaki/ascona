// code by jph
package ch.alpine.ascona.dat;

import ch.alpine.bridge.res.ResourceMapper;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

public class GokartPos extends ResourceMapper {
  private static final String INDEX = "/ch/alpine/ascona/gokart/tpq/resource_index.vector";

  public GokartPos() {
    super(INDEX);
  }

  private static final TensorUnaryOperator EXTRACT = row -> row.extract(1, 4);

  @Override
  public Tensor apply(Tensor tensor) {
    return EXTRACT.slash(tensor);
  }
}
