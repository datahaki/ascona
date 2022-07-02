// code by jph
package ch.alpine.ascona.util.ref;

import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class SpaceParam {
  private final List<ManifoldDisplays> list;
  /** currently selected */
  @FieldSelectionCallback("getList")
  public ManifoldDisplays manifoldDisplays;

  public SpaceParam(List<ManifoldDisplays> list) {
    this.list = list;
    manifoldDisplays = list.get(0);
  }

  public List<ManifoldDisplays> getList() {
    return list;
  }
}