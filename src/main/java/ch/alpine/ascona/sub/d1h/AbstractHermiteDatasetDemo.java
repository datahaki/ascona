// code by jph
package ch.alpine.ascona.sub.d1h;

import java.util.List;

import ch.alpine.ascona.dat.GokartPoseDatas;
import ch.alpine.ascona.ref.GokartPosVelParam;
import ch.alpine.ascony.dat.Se2PosVel;
import ch.alpine.ascony.dat.Se2PosVelHz;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.Thinning;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.qty.UnitSystem;

abstract class AbstractHermiteDatasetDemo extends ManifoldDisplayDemo {
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "1", "2", "5", "10", "25", "50" })
    public Integer skips = 5;
  }

  private final GokartPosVelParam gokartPosVelParam;
  protected final Param param;
  private Se2PosVelHz posVelHz;
  protected Tensor _control = Tensors.empty();

  public AbstractHermiteDatasetDemo(Object object) {
    super(gokartPosVelParam = new GokartPosVelParam(), param = new Param(), object);
    fieldsEditor(gokartPosVelParam).addUniversalListener(this::updateState);
    fieldsEditor(param).addUniversalListener(this::updateState);
    addChangeListener(this::updateState);
    geometricComponent().setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
    updateState();
  }

  private final void updateState() {
    // IO.println("update state");
    posVelHz = gokartPosVelParam.getPosVelHz();
    Se2PosVel posVelSequence = posVelHz.se2PosVel();
    _control = Thinning.of(posVelSequence.getHermiteControlPoints(manifoldDisplay()), param.skips);
  }

  protected final Scalar getDelta() {
    return UnitSystem.SI().apply(RealScalar.of(param.skips).divide(posVelHz.samplingRate()));
  }

  @Override
  protected final List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_R2;
  }
}
