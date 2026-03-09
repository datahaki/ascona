// code by jph
package ch.alpine.ascona.dat.gok;

import java.util.List;

import ch.alpine.ascony.res.ResourceMapper;
import ch.alpine.tensor.Tensor;

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

  public List<String> keys() {
    return resourceMapper.list();
  }

  public PosVelHz get(String key, int limit) {
    return new PosVelHz(Tensor.of(resourceMapper.importResource(key).stream().limit(limit)));
  }
}
