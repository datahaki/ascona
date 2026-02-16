// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ControlPointsStatic;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.ts.Transition;
import ch.alpine.sophis.ts.TransitionSpace;
import ch.alpine.sophus.math.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.hun.BipartiteMatching;
import ch.alpine.tensor.pdf.RandomSample;

public class BipartiteMatchingDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.d2Rasters());
    }

    @FieldClip(min = "1", max = "200")
    public Integer n = 5;
    @FieldFuse
    public transient Boolean shuffle = true;
  }

  private final Param param;
  private Tensor ground;

  public BipartiteMatchingDemo() {
    this(new Param());
  }

  public BipartiteMatchingDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
    controlPointsRender.setMidpointIndicated(false);
  }

  private void shuffle() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    int n = param.n;
    ground = RandomSample.of(manifoldDisplay.randomSampleInterface(), n);
    Tensor tensor = RandomSample.of(manifoldDisplay.randomSampleInterface(), n);
    Tensor xyas = Tensor.of(tensor.stream().map(manifoldDisplay::point2xya));
    controlPointsRender.setControlPointsSe2(xyas);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = controlPointsRender.getGeodesicControlPoints();
    if (0 < control.length()) {
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
      Tensor matrix = StaticHelper.distanceMatrix(manifold, control, ground);
      BipartiteMatching bipartiteMatching = BipartiteMatching.of(matrix);
      int[] matching = bipartiteMatching.matching();
      graphics.setColor(Color.RED);
      TransitionSpace transitionSpace = manifoldDisplay.transitionSpace();
      for (int index = 0; index < matching.length; ++index)
        if (matching[index] != BipartiteMatching.UNASSIGNED) {
          Tensor head = control.get(index);
          Tensor tail = ground.get(matching[index]);
          Transition transition = transitionSpace.connect(head, tail);
          Path2D path2d = geometricLayer.toPath2D(transition.linearized(RealScalar.of(0.1)));
          graphics.draw(path2d);
        }
    }
    ControlPointsStatic.gray(manifoldDisplay(), ground).render(geometricLayer, graphics);
  }

  static void main() {
    launch();
  }
}
