// code by jph
package ch.alpine.ascona.ref.d1h;

import java.util.List;

import ch.alpine.ascona.dat.gok.GokartPosVelParam;
import ch.alpine.ascona.dat.gok.GokartPoseDatas;
import ch.alpine.ascona.dat.gok.PosVelHz;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.Thinning;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

abstract class AbstractHermiteDatasetDemo extends ManifoldDisplayDemo {
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "1", "2", "5", "10", "25", "50" })
    public Integer skips = 5;
  }

  private final GokartPosVelParam gokartPosVelParam;
  protected final Param param;
  protected Tensor _control = Tensors.empty();

  public AbstractHermiteDatasetDemo(Object object) {
    super(gokartPosVelParam = new GokartPosVelParam(), param = new Param(), object);
    fieldsEditor(gokartPosVelParam).addUniversalListener(this::updateState);
    fieldsEditor(param).addUniversalListener(this::updateState);
    geometricComponent().setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
    updateState();
  }

  private final void updateState() {
    PosVelHz posVelHz = gokartPosVelParam.getPosVelHz();
    _control = Thinning.of(posVelHz.getPosVelSequence(), param.skips);
  }

  @Override
  protected final List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_ONLY;
  }
}
