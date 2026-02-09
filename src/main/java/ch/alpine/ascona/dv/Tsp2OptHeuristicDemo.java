// code by jph
package ch.alpine.ascona.dv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.fit.IntUndirectedEdge;
import ch.alpine.sophis.fit.MinimumSpanningTree;
import ch.alpine.sophis.fit.Tsp2OptHeuristic;
import ch.alpine.sophis.ts.TransitionSpace;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.sca.Round;
import ch.alpine.tensor.sca.exp.Log;

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
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    RenderQuality.setQuality(graphics);
    graphics.setColor(Color.BLACK);
    graphics.drawString(tsp2OptHeuristic.cost().map(Round._5).toString(), 3, 450);
    Tensor sequence = getGeodesicControlPoints();
    {
      // TODO ASCONA should use transition !?
      Tensor domain = Subdivide.of(0.0, 1.0, 10);
      graphics.setColor(new Color(128, 128, 128, 128));
      graphics.setStroke(new BasicStroke(1f));
      for (IntUndirectedEdge directedEdge : list) {
        Tensor p = sequence.get(directedEdge.i());
        Tensor q = sequence.get(directedEdge.j());
        ScalarTensorFunction curve = geodesicSpace.curve(p, q);
        Tensor tensor = Tensor.of(domain.map(curve).stream().map(manifoldDisplay::point2xy));
        Path2D line = geometricLayer.toPath2D(tensor);
        graphics.draw(line);
      }
    }
    Color color = Color.BLACK;
    graphics.setColor(color);
    for (Tensor p : sequence) {
      Point2D point2d = geometricLayer.toPoint2D(manifoldDisplay.point2xy(p));
      graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 3, 3);
    }
    int[] index = tsp2OptHeuristic.index();
    TransitionSpace transitionSpace = manifoldDisplay.transitionSpace();
    graphics.setColor(new Color(0, 192, 192));
    graphics.setStroke(new BasicStroke(1.5f));
    for (int i = 0; i < index.length; ++i) {
      Tensor head = sequence.get(index[i]);
      Tensor tail = sequence.get(index[(i + 1) % index.length]);
      Tensor tensor = transitionSpace.connect(head, tail).linearized(RealScalar.of(0.1));
      Path2D line = geometricLayer.toPath2D(tensor);
      graphics.draw(line);
    }
    {
      Show show = new Show();
      show.add(ListLinePlot.of(points));
      show.render_autoIndent(graphics, new Rectangle(0, 0, 300, 200));
    }
    {
      Show show = new Show();
      show.add(ListLinePlot.of(Tensor.of(points.stream().map(v -> Tensors.of(v.Get(0), v.Get(1).map(Log.FUNCTION))))));
      show.render_autoIndent(graphics, new Rectangle(0, 200, 300, 200));
    }
  }

  private List<IntUndirectedEdge> list = new LinkedList<>();

  private void shuffle() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    Tensor sample = RandomSample.of(randomSampleInterface, param0.numel);
    setControlPointsSe2(Tensor.of(sample.stream().map(manifoldDisplay::point2xya)));
    // ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Tensor matrix = StaticHelper.distanceMatrix(manifold, getGeodesicControlPoints());
    list = MinimumSpanningTree.of(matrix);
    tsp2OptHeuristic = new Tsp2OptHeuristic(matrix);
    points = Tensors.empty();
  }

  static void main() {
    launch();
  }
}
