// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.LieGroupOps;
import ch.alpine.sophus.math.api.TensorMapping;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Timing;

public class Se2AnimationDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.SE2C_SE2);
      drawControlPoints = false;
    }
  }

  private final Param param;
  private final JToggleButton jToggleAnimate = new JToggleButton("animate");
  private final Timing timing = Timing.started();
  // ---
  private Tensor snapshotUncentered;
  private Tensor snapshot;

  public Se2AnimationDemo() {
    this(new Param());
  }

  public Se2AnimationDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    {
      jToggleAnimate.addActionListener(e -> {
        if (jToggleAnimate.isSelected()) {
          snapshotUncentered = getControlPointsSe2();
          Tensor sequence = getGeodesicControlPoints();
          if (0 < sequence.length()) {
            Tensor origin = sequence.get(0);
            LieGroup lieGroup = (LieGroup) manifoldDisplay().geodesicSpace();
            Tensor shift = lieGroup.element(origin).inverse().toCoordinate();
            snapshot = new LieGroupOps(lieGroup).actionL(shift).slash(sequence);
          }
        } else
          setControlPointsSe2(snapshotUncentered);
      });
      timerFrame.jToolBar.add(jToggleAnimate);
    }
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1.5, -1, -1}, {2, 1, 1}, {-0.5, 1.5, 2}, {-1, -1.5, -2}, {-1.5, 0, 0.3}}"));
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
    LieGroup lieGroup = (LieGroup) manifoldDisplay().geodesicSpace();
    LieGroupOps lieGroupOps = new LieGroupOps(lieGroup);
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      if (jToggleAnimate.isSelected())
        setControlPointsSe2(lieGroupOps.conjugation(random(10 + timing.seconds() * 0.1, 0)).slash(snapshot));
      RenderQuality.setQuality(graphics);
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      LeversHud.render( //
          Biinvariants.METRIC, //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics));
      TensorMapping actionL = lieGroupOps.actionL(Tensors.vector(7, 0, 0));
      LeversHud.render( //
          Biinvariants.LEVERAGES, //
          LeversRender.of(manifoldDisplay, actionL.slash(sequence), actionL.apply(origin), geometricLayer, graphics));
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
