// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Random;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.crv.TransitionSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.ts.Tsp2OptHeuristic;

public class Tsp2OptHeuristicDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.metricManifolds());
      manifoldDisplays = ManifoldDisplays.R2;
    }

    @FieldSelectionArray({ "25", "50", "100", "150", "200" })
    public Integer numel = 50;
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param1 {
    public Integer attempts = 20;
    public Boolean active = false;
  }

  private final Random random = new Random();
  private final Param0 param0;
  private final Param1 param1;
  private Tsp2OptHeuristic tsp2OptHeuristic;
  private Tensor points = Tensors.empty();

  public Tsp2OptHeuristicDemo() {
    this(new Param0(), new Param1());
  }

  public Tsp2OptHeuristicDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    controlPointsRender.setPositioningEnabled(false);
    fieldsEditor(0).addUniversalListener(this::shuffle);
    // ---
    shuffle();
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param1.active) {
      for (int i = 0; i < param1.attempts; ++i)
        tsp2OptHeuristic.next();
      points.append(Tensors.of(RealScalar.of(points.length()), tsp2OptHeuristic.cost()));
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setQuality(graphics);
    Tensor sequence = getGeodesicControlPoints();
    Color color = Color.BLACK;
    graphics.setColor(color);
    for (Tensor p : sequence) {
      Point2D point2d = geometricLayer.toPoint2D(manifoldDisplay.point2xy(p));
      graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 3, 3);
    }
    int[] index = tsp2OptHeuristic.index();
    TransitionSpace transitionSpace = manifoldDisplay.transitionSpace();
    graphics.setColor(Color.CYAN);
    for (int i = 0; i < index.length; ++i) {
      Tensor head = sequence.get(index[i]);
      Tensor tail = sequence.get(index[(i + 1) % index.length]);
      Tensor tensor = transitionSpace.connect(head, tail).linearized(RealScalar.of(0.1));
      Path2D line = geometricLayer.toPath2D(tensor);
      graphics.draw(line);
    }
    Show show = new Show();
    show.add(ListPlot.of(points));
    show.render(graphics, new Rectangle(0, 0, 300, 200));
  }

  private void shuffle() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    Tensor sample = RandomSample.of(randomSampleInterface, random, param0.numel);
    setControlPointsSe2(Tensor.of(sample.stream().map(manifoldDisplay::point2xya)));
    // ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Tensor matrix = StaticHelper.distanceMatrix(manifold, getGeodesicControlPoints());
    tsp2OptHeuristic = new Tsp2OptHeuristic(matrix, random);
    points = Tensors.empty();
  }

  public static void main(String[] args) {
    launch();
  }
}
