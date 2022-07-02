// code by jph
package ch.alpine.ascona.util.ref;

import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class AsconaParam {
  public final SpaceParam spaceParam;
  public final boolean addRemoveControlPoints;

  public AsconaParam(boolean addRemoveControlPoints, List<ManifoldDisplays> list) {
    spaceParam = new SpaceParam(list);
    this.addRemoveControlPoints = addRemoveControlPoints;
  }
}
