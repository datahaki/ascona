// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.lie.Symmetrize;
import ch.alpine.tensor.mat.SymmetricMatrixQ;
import ch.alpine.tensor.opt.ts.Tsp2OptHeuristic;

public class Tsp2OptHeuristicDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.metricManifolds());
      manifoldDisplays = ManifoldDisplays.R2;
    }

    @FieldInteger
    @FieldSelectionArray({ "25", "50", "100", "150", "200" })
    public Scalar numel = RealScalar.of(50);
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param1 {
    @FieldInteger
    public Scalar attempts = RealScalar.of(20);
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
      for (int i = 0; i < param1.attempts.number().intValue(); ++i)
        tsp2OptHeuristic.next();
      points.append(Tensors.of(RealScalar.of(points.length()), tsp2OptHeuristic.cost()));
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    RenderQuality.setQuality(graphics);
    Tensor sequence = getGeodesicControlPoints();
    Color color = Color.BLACK;
    graphics.setColor(color);
    for (Tensor p : sequence) {
      Point2D point2d = geometricLayer.toPoint2D(manifoldDisplay.point2xy(p));
      graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 3, 3);
    }
    // for (int index = 0; index < sequence.length(); ++index) {
    // PointsRender pointsRender = new PointsRender(color, color);
    // pointsRender.show(manifoldDisplay()::matrixLift, getControlPointShape(), Tensors.of(sequence.get(index))).render(geometricLayer, graphics);
    // }
    int[] index = tsp2OptHeuristic.index();
    // TODO ASCONA is there a smart way to select how to draw lines
    int res = manifoldDisplay.equals(R2Display.INSTANCE) ? 1 : 10;
    Tensor domain = Subdivide.of(0.0, 1.0, res);
    graphics.setColor(Color.CYAN);
    for (int i = 0; i < index.length; ++i) {
      Tensor p = sequence.get(index[i]);
      Tensor q = sequence.get(index[(i + 1) % index.length]);
      ScalarTensorFunction curve = homogeneousSpace.curve(p, q);
      Tensor tensor = Tensor.of(domain.map(curve).stream().map(manifoldDisplay::point2xy));
      Path2D line = geometricLayer.toPath2D(tensor);
      graphics.draw(line);
    }
    VisualSet visualSet = new VisualSet();
    visualSet.add(points);
    JFreeChart jFreeChart = ListPlot.of(visualSet, true);
    jFreeChart.draw(graphics, new Rectangle2D.Double(0, 0, 300, 200));
  }

  public Tensor distanceMatrix(Tensor sequence) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Sedarim sedarim = Biinvariants.METRIC.ofSafe(manifold).distances(sequence);
    Tensor matrix = Tensor.of(sequence.stream().map(sedarim::sunder));
    return SymmetricMatrixQ.of(matrix) //
        ? matrix
        : Symmetrize.of(matrix);
  }

  private void shuffle() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    Tensor sample = RandomSample.of(randomSampleInterface, random, param0.numel.number().intValue());
    setControlPointsSe2(Tensor.of(sample.stream().map(manifoldDisplay::point2xya)));
    Tensor matrix = distanceMatrix(getGeodesicControlPoints());
    tsp2OptHeuristic = new Tsp2OptHeuristic(matrix, random);
    points = Tensors.empty();
  }

  public static void main(String[] args) {
    launch();
  }
}
