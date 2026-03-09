// code by jph
package ch.alpine.ascona.dat.gok;

import java.util.List;

import ch.alpine.ascony.res.ResourceMapper;
import ch.alpine.tensor.Tensor;

public enum GokartPos {
  INSTANCE;

  private final ResourceMapper resourceMapper = //
      ResourceMapper.of("/ch/alpine/ascona/gokart/resource_index.vector");

  public List<String> keys() {
    return resourceMapper.list();
  }

  public PosHz get(String key, int limit) {
    return new PosHz(Tensor.of(resourceMapper.importResource(key).stream().limit(limit)));
  }
}
