// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversHud;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.ascony.win.PlaceWrap;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.noise.SimplexContinuousNoise;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;

class Se2AnimationDemo extends ControlPointsDemo {
  private final JToggleButton jToggleAnimate = new JToggleButton("animate");
  private final Timing timing = Timing.started();
  // ---
  private Tensor snapshotUncentered;
  private Tensor snapshot;

  public Se2AnimationDemo() {
    jToggleAnimate.addActionListener(_ -> {
      if (jToggleAnimate.isSelected()) {
        snapshotUncentered = getControlPointsSe2();
        Tensor sequence = getGeodesicControlPoints();
        if (0 < sequence.length()) {
          Tensor origin = sequence.get(0);
          LieGroup lieGroup = manifoldDisplay().lieGroup();
          Tensor shift = lieGroup.invert(origin);
          snapshot = Tensor.of(sequence.stream().map(lieGroup.actionL(shift)));
        }
      } else
        setControlPointsSe2(snapshotUncentered);
    });
    timerFrame.jToolBar.add(jToggleAnimate);
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1.5, -1, -1}, {2, 1, 1}, {-0.5, 1.5, 2}, {-1, -1.5, -2}, {-1.5, 0, 0.3}}"));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_SE2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.DELEGATED;
  }

  private static Tensor random(double toc, int index) {
    return Tensors.vector( //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 0), //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 1), //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 2));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LieGroup lieGroup = manifoldDisplay().lieGroup();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      if (jToggleAnimate.isSelected()) {
        Tensor newPoints = Tensor
            .of(snapshot.stream().map(lieGroup.conjugation(random(10 + timing.seconds().multiply(Quantity.of(0.1, "s^-1")).number().doubleValue(), 0))));
        setControlPointsSe2(newPoints);
      }
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      LeversHud.render( //
          Biinvariants.METRIC, //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics));
      TensorUnaryOperator actionL = lieGroup.actionL(Tensors.vector(7, 0, 0));
      LeversHud.render( //
          Biinvariants.LEVERAGES, //
          LeversRender.of(manifoldDisplay, Tensor.of(sequence.stream().map(actionL)), actionL.apply(origin), geometricLayer, graphics));
    }
  }

  static void main() {
    new Se2AnimationDemo().runStandalone();
  }
}
