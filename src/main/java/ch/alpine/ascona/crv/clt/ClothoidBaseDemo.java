// code by jph
package ch.alpine.ascona.crv.clt;

import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.win.ControlPointsDemo;

abstract class ClothoidBaseDemo extends ControlPointsDemo {
  public ClothoidBaseDemo(Object... objects) {
    super(objects);
  }

  @Override
  protected final List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.CLC_ONLY;
  }
}
