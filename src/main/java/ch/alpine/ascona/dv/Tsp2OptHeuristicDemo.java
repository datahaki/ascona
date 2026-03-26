// code by jph
package ch.alpine.ascona.dv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.ref.BiinvariantsParam;
import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.fig.plt.MatrixPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.fit.IntUndirectedEdge;
import ch.alpine.sophis.fit.MinimumSpanningTree;
import ch.alpine.sophis.fit.Tsp2OptHeuristic;
import ch.alpine.sophis.ts.Transition;
import ch.alpine.sophis.ts.TransitionSpace;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.sca.Round;

class Tsp2OptHeuristicDemo extends ManifoldDisplayDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "25", "50", "100", "150", "200", "500" })
    public Integer numel = 50;
    public final ShuffleFuse shuffleFuse = new ShuffleFuse();
  }

  @ReflectionMarker
  static class Param2 {
    @FieldSelectionArray({ "10", "20", "40" })
    public Integer factor = 10;
    @FieldFuse
    public transient Boolean active = false;
  }

  private final Param0 param0;
  public final BiinvariantsParam biinvariantsParam;
  private final Param2 param2;
  // ---
  private Tensor control;
  private Tensor matrix;
  private List<IntUndirectedEdge> list;
  private Tsp2OptHeuristic tsp2OptHeuristic;
  /* points for plotting */
  private Tensor points = Tensors.empty();
  private int total = 0;

  public Tsp2OptHeuristicDemo() {
    super(param0 = new Param0(), biinvariantsParam = BiinvariantsParam.okay(), param2 = new Param2());
    fieldsEditor(param0).addUniversalListener(this::shuffle);
    fieldsEditor(biinvariantsParam).addUniversalListener(this::distances);
    addChangeListener(this::shuffle);
    shuffle();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  private void shuffle() {
    total = 0;
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    control = RandomSample.of(randomSampleInterface, param0.numel);
    tsp2OptHeuristic = null;
    distances();
    points = Tensors.empty();
  }

  private void distances() {
    Manifold manifold = manifoldDisplay().manifold();
    matrix = StaticHelper.distanceMatrix_symmetrized(biinvariantsParam.ofSafe(manifold), control);
    list = MinimumSpanningTree.of(matrix);
    tsp2OptHeuristic = Objects.isNull(tsp2OptHeuristic) //
        ? Tsp2OptHeuristic.of(matrix)
        : new Tsp2OptHeuristic(matrix, tsp2OptHeuristic.index());
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param2.active) {
      boolean improved = false;
      int m = control.length() * param2.factor;
      for (int i = 0; i < m; ++i) {
        improved |= tsp2OptHeuristic.next();
        ++total;
      }
      param2.active = improved;
      points.append(Tensors.of(RealScalar.of(points.length()), tsp2OptHeuristic.cost()));
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    TransitionSpace transitionSpace = manifoldDisplay.transitionSpace();
    graphics.setColor(Color.BLACK);
    graphics.drawString(tsp2OptHeuristic.cost().maps(Round._5).toString(), 3, 450);
    Tensor sequence = control;
    graphics.setColor(new Color(128, 128, 128, 128));
    graphics.setStroke(new BasicStroke());
    for (IntUndirectedEdge directedEdge : list) {
      Tensor p = sequence.get(directedEdge.i());
      Tensor q = sequence.get(directedEdge.j());
      Transition transition = transitionSpace.connect(p, q);
      Tensor linearized = transition.linearized(RealScalar.ONE);
      Tensor tensor = manifoldDisplay.point2xy().slash(linearized);
      Path2D line = geometricLayer.toPath2D(tensor);
      graphics.draw(line);
    }
    manifoldDisplay.showPoints(ColorPairs.REFERENCE, RealScalar.of(0.3), sequence) //
        .render(geometricLayer, graphics);
    int[] index = tsp2OptHeuristic.index();
    graphics.setColor(new Color(0, 192, 192));
    graphics.setStroke(new BasicStroke(1.5f));
    TensorUnaryOperator tuo = manifoldDisplay.point2xy();
    for (int i = 0; i < index.length; ++i) {
      Tensor head = sequence.get(index[i]);
      Tensor tail = sequence.get(index[(i + 1) % index.length]);
      Tensor tensor = transitionSpace.connect(head, tail).linearized(RealScalar.ONE);
      Path2D line = geometricLayer.toPath2D(tuo.slash(tensor));
      graphics.draw(line);
    }
    Dimension dimension = geometricComponent().getSize();
    dimension.width /= 2;
    dimension.height /= 2;
    {
      Show show = new Show();
      show.setShowLabel("Distance matrix");
      show.add(MatrixPlot.of(matrix));
      show.render_autoIndent(graphics, new Rectangle(dimension.width, 0, dimension.width, dimension.height));
    }
    {
      Show show = new Show();
      show.setShowLabel("Route length (search=" + total + ")");
      show.add(ListLinePlot.of(points));
      show.render_autoIndent(graphics, new Rectangle(dimension.width, dimension.height, dimension.width, dimension.height));
    }
  }

  static void main() {
    new Tsp2OptHeuristicDemo().runStandalone();
  }
}
