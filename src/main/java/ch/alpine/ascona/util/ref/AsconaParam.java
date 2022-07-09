// code by jph
package ch.alpine.ascona.util.ref;

import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class AsconaParam extends SpaceParam {
  public final boolean addRemoveControlPoints;
  public boolean drawControlPoints = true;

  public AsconaParam(boolean addRemoveControlPoints, List<ManifoldDisplays> list) {
    super(list);
    this.addRemoveControlPoints = addRemoveControlPoints;
  }
}
