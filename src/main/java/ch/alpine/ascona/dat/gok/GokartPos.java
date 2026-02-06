// code by jph
package ch.alpine.ascona.dat.gok;

import java.util.List;

import ch.alpine.bridge.res.ResourceMapper;
import ch.alpine.tensor.Tensor;

public enum GokartPos {
  ;
  private static final ResourceMapper RESOURCE_MAPPER = //
      ResourceMapper.of("/ch/alpine/ascona/gokart/tpq/resource_index.vector");

  public static List<String> list() {
    return RESOURCE_MAPPER.list();
  }

  public static PosHz get(String key, int limit) {
    return new PosHz(Tensor.of(RESOURCE_MAPPER.importResource(key).stream().limit(limit)));
  }
}
