// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.api.ControlPointsRender;
import ch.alpine.ascona.util.api.ControlPointsRenders;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.opt.hun.BipartiteMatching;

// TODO ASCONA generalize demo for 2 scattered sets on a manifold
public class BipartiteMatchingDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldInteger
    @FieldClip(min = "1", max = "10")
    public Scalar n = RealScalar.of(5);
    @FieldInteger
    @FieldClip(min = "1", max = "10")
    public Scalar m = RealScalar.of(3);
    @FieldFuse("shuffle")
    public transient Boolean shuffle = true;
  }

  private final ControlPointsRender controlPointsRender;
  private final Param param;

  public BipartiteMatchingDemo() {
    this(new Param());
  }

  public BipartiteMatchingDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender = ControlPointsRenders.create( //
        true, () -> ManifoldDisplays.R2.manifoldDisplay(), timerFrame.geometricComponent);
    fieldsEditor.addUniversalListener(() -> {
      ManifoldDisplay manifoldDisplay = ManifoldDisplays.R2.manifoldDisplay();
      Tensor tensor = RandomSample.of(manifoldDisplay.randomSampleInterface(), param.m.number().intValue());
      Tensor xyas = Tensor.of(tensor.stream().map(manifoldDisplay::point2xya));
      controlPointsRender.setControlPointsSe2(xyas);
    });
    controlPointsRender.setControlPointsSe2(Tensors.fromString("{{1, 0, 0}, {0, 1, 0}, {1, 1, 0}}"));
    controlPointsRender.setMidpointIndicated(false);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = controlPointsRender.getGeodesicControlPoints();
    if (0 < control.length()) {
      Tensor CIRCLE = CirclePoints.of(param.n.number().intValue()).multiply(RealScalar.of(2));
      new PathRender(Color.GRAY).setCurve(CIRCLE, true).render(geometricLayer, graphics);
      Tensor matrix = Outer.of(Vector2Norm::between, control, CIRCLE);
      BipartiteMatching bipartiteMatching = BipartiteMatching.of(matrix);
      int[] matching = bipartiteMatching.matching();
      graphics.setColor(Color.RED);
      for (int index = 0; index < matching.length; ++index)
        if (matching[index] != BipartiteMatching.UNASSIGNED) {
          Path2D path2d = geometricLayer.toPath2D(Tensors.of(control.get(index), CIRCLE.get(matching[index])));
          graphics.draw(path2d);
        }
    }
    {
      LeversRender leversRender = LeversRender.of(ManifoldDisplays.R2.manifoldDisplay(), control, null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new BipartiteMatchingDemo().setVisible(1000, 600);
  }
}
