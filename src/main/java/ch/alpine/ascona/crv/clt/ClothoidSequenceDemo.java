// code by jph
package ch.alpine.ascona.crv.clt;

import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;

abstract class ClothoidSequenceDemo extends ControlPointsDemo {
  public ClothoidSequenceDemo(Object... objects) {
    super(objects);
  }

  @Override
  protected final List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.CLC_ONLY;
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointTypes.CURVYCURV;
  }
}
