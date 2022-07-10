// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.Optional;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.lie.so3.Rodrigues;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.lie.r2.RotationMatrix;

public class AnimationDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.R2_S2);
    }

    public Biinvariants biinvariants = Biinvariants.METRIC;
  }

  @ReflectionMarker
  public static class Param1 {
    public Boolean animate = false;
  }

  private final Param0 param0;
  private final Param1 param1;
  private final Timing timing = Timing.started();
  // ---
  private Tensor snapshotUncentered;
  private Tensor snapshot;

  public AnimationDemo() {
    this(new Param0(), new Param1());
  }

  public AnimationDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    controlPointsRender.setMidpointIndicated(false);
    fieldsEditor(1).addUniversalListener(() -> {
      if (param1.animate) {
        snapshotUncentered = getControlPointsSe2();
        Tensor sequence = getGeodesicControlPoints();
        if (0 < sequence.length()) {
          snapshot = snapshotUncentered.copy();
        }
      } else
        setControlPointsSe2(snapshotUncentered);
    });
    setControlPointsSe2(Tensors.matrix(new Number[][] { //
        { 0.000, 0.000, 0.000 }, { 0.699, -0.521, 0.000 }, { 0.641, 0.634, 0.000 }, //
        { -0.320, 0.734, 0 }, { -0.067, -0.716, 0.000 }, { -0.768, 0.215, 0.000 } }));
  }

  private static Tensor random(double toc, int index) {
    return Tensors.vector( //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 0), //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 1), //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 2)).multiply(RealScalar.of(1));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      if (param1.animate) {
        Tensor list = Tensors.empty();
        // TODO ASCONA should be part of manifoldDisplay interface
        switch (param0.manifoldDisplays) {
        case R2: {
          Tensor vectorExp = RotationMatrix.of(timing.seconds() * 0.2);
          for (Tensor xya : snapshot) {
            Tensor project = vectorExp.dot(manifoldDisplay.xya2point(xya));
            list.append(manifoldDisplay.point2xya(project));
          }
          break;
        }
        case S2: {
          Tensor vector = random(10 + timing.seconds() * 0.1, 0);
          Tensor vectorExp = Rodrigues.vectorExp(vector);
          for (Tensor xya : snapshot) {
            Tensor project = vectorExp.dot(manifoldDisplay.xya2point(xya));
            list.append(manifoldDisplay.point2xya(project));
          }
          break;
        }
        }
        setControlPointsSe2(list);
      }
      RenderQuality.setQuality(graphics);
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      LeversHud.render(param0.biinvariants, leversRender);
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
