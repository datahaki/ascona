// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.util.DisjointSets;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.fit.MinimumSpanningTree;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.IntUndirectedEdge;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.Symmetrize;
import ch.alpine.tensor.mat.SymmetricMatrixQ;

public class MinimumSpanningTreeDemo extends ControlPointsDemo {
  private static record EdgeComparator(Tensor matrix) implements Comparator<IntUndirectedEdge> {
    @Override
    public int compare(IntUndirectedEdge edge1, IntUndirectedEdge edge2) {
      return Scalars.compare( //
          edge1.Get(matrix), //
          edge2.Get(matrix));
    }
  }

  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.manifolds());
      manifoldDisplays = ManifoldDisplays.R2;
      drawControlPoints = false;
    }

    @FieldInteger
    @FieldSelectionArray({ "10", "20", "30" })
    public Scalar size = RealScalar.of(20);
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param1 {
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldInteger
    @FieldClip(min = "1", max = "8")
    public Scalar refine = RealScalar.of(2);
    public ColorDataLists colorDataLists = ColorDataLists._097;
  }

  private final Param0 param0;
  private final Param1 param1;

  public MinimumSpanningTreeDemo() {
    this(new Param0(), new Param1());
  }

  public MinimumSpanningTreeDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    // ---
    controlPointsRender.setMidpointIndicated(false);
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    int n = param0.size.number().intValue();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::point2xya));
    setControlPointsSe2(tensor);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    RenderQuality.setQuality(graphics);
    Tensor sequence = getGeodesicControlPoints();
    Tensor domain = Subdivide.of(0.0, 1.0, 10);
    final int splits = param1.refine.number().intValue();
    DisjointSets disjointSets = DisjointSets.allocate(sequence.length());
    if (0 < sequence.length()) {
      Tensor matrix = distanceMatrix(manifold, sequence);
      List<IntUndirectedEdge> list = MinimumSpanningTree.of(matrix);
      Collections.sort(list, new EdgeComparator(matrix));
      int count = Math.max(0, list.size() - splits);
      {
        for (IntUndirectedEdge directedEdge : list.subList(0, count))
          disjointSets.union(directedEdge.i(), directedEdge.j());
      }
      graphics.setColor(Color.BLACK);
      for (IntUndirectedEdge directedEdge : list.subList(0, count)) {
        Tensor p = sequence.get(directedEdge.i());
        Tensor q = sequence.get(directedEdge.j());
        ScalarTensorFunction curve = geodesicSpace.curve(p, q);
        Tensor tensor = Tensor.of(domain.map(curve).stream().map(manifoldDisplay::point2xy));
        Path2D line = geometricLayer.toPath2D(tensor);
        graphics.draw(line);
      }
    }
    Map<Integer, Integer> map = disjointSets.createMap(new AtomicInteger()::getAndIncrement);
    Tensor shape = manifoldDisplay.shape();
    for (int index = 0; index < sequence.length(); ++index) {
      int unique = map.get(disjointSets.key(index));
      Color color = param1.colorDataLists.cyclic().getColor(unique);
      PointsRender pointsRender = new PointsRender(color, color);
      pointsRender.show(manifoldDisplay::matrixLift, shape, Tensors.of(sequence.get(index))).render(geometricLayer, graphics);
    }
  }

  public Tensor distanceMatrix(Manifold manifold, Tensor sequence) {
    Sedarim sedarim = param1.biinvariants.ofSafe(manifold).distances(sequence);
    Tensor matrix = Tensor.of(sequence.stream().map(sedarim::sunder));
    return SymmetricMatrixQ.of(matrix) //
        ? matrix
        : Symmetrize.of(matrix);
  }

  public static void main(String[] args) {
    launch();
  }
}
