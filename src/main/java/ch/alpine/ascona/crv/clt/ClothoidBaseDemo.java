// code by jph
package ch.alpine.ascona.crv.clt;

import java.util.Collection;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;

abstract class ClothoidBaseDemo extends ControlPointsDemo {
  public ClothoidBaseDemo(Object... objects) {
    super(objects);
  }

  @Override
  protected final Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.CLC_ONLY;
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }
}
