// code by jph
package ch.alpine.ascona.dat.gok;

import java.util.List;

import ch.alpine.bridge.res.ResourceMapper;
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
  ;
  private static final ResourceMapper RESOURCE_MAPPER = //
      ResourceMapper.of("/ch/alpine/ascona/gokart/tpqv/resource_index.vector");

  public static List<String> list() {
    return RESOURCE_MAPPER.list();
  }

  public static PosVelHz get(String key, int limit) {
    return new PosVelHz(Tensor.of(RESOURCE_MAPPER.importResource(key).stream().limit(limit)));
  }
}
