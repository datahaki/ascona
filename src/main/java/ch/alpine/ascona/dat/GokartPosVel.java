// code by jph
package ch.alpine.ascona.dat;

import java.util.List;

import ch.alpine.ascony.dat.ControlPosVelSe2;
import ch.alpine.ascony.dat.ControlPosVelSe2Hz;
import ch.alpine.ascony.res.ResourceMapper;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.red.Mean;

/** Columns:
 * time
 * px
 * py
 * pangle
 * quality
 * vx
 * vy
 * vangle */
public enum GokartPosVel {
  INSTANCE;

  private final ResourceMapper resourceMapper = //
      ResourceMapper.of("ch/alpine/ascona/gokart/tpqv/resource_index.vector");
  private final TensorUnaryOperator EXTRACT_POS = row -> row.extract(1, 4);
  private final TensorUnaryOperator EXTRACT_VEL = row -> row.extract(5, 8).maps(s -> Quantity.of(s, "s^-1"));
  private final TensorUnaryOperator EXTRACT = row -> Tensors.of( //
      EXTRACT_POS.apply(row), //
      EXTRACT_VEL.apply(row));

  public List<String> keys() {
    return resourceMapper.list();
  }

  public ControlPosVelSe2Hz get(String key, int limit) {
    Tensor tensor = Tensor.of(resourceMapper.importResource(key).stream().limit(limit));
    Scalar samplingRate = Quantity.of(Mean.ofVector(Differences.of(tensor.get(Tensor.ALL, 0))).reciprocal(), "Hz");
    return new ControlPosVelSe2Hz(new ControlPosVelSe2(EXTRACT.slash(tensor)), samplingRate);
  }
}
