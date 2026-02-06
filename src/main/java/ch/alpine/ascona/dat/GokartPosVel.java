// code by jph
package ch.alpine.ascona.dat;

import ch.alpine.bridge.res.ResourceMapper;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;

/** Columns:
 * time
 * px
 * py
 * pangle
 * quality
 * vx
 * vy
 * vangle */
public class GokartPosVel extends ResourceMapper {
  private static final String INDEX = "/ch/alpine/ascona/gokart/tpqv/resource_index.vector";

  public GokartPosVel() {
    super(INDEX);
  }

  private static final TensorUnaryOperator EXTRACT = row -> Tensors.of(row.extract(1, 4), row.extract(5, 8));

  @Override
  public Tensor apply(Tensor tensor) {
    return EXTRACT.slash(tensor);
  }
}
