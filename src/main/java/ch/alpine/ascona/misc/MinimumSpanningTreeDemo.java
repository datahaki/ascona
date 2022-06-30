// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ch.alpine.ascona.lev.LogWeightingDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.util.DisjointSets;
import ch.alpine.sophus.fit.MinimumSpanningTree;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.IntUndirectedEdge;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.Symmetrize;
import ch.alpine.tensor.mat.SymmetricMatrixQ;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public class MinimumSpanningTreeDemo extends LogWeightingDemo {
  private static record EdgeComparator(Tensor matrix) implements Comparator<IntUndirectedEdge> {
    @Override
    public int compare(IntUndirectedEdge edge1, IntUndirectedEdge edge2) {
      return Scalars.compare( //
          edge1.Get(matrix), //
          edge2.Get(matrix));
    }
  }

  // TODO ASCONA DEMO manage by reflection
  final SpinnerLabel<Integer> spinnerRefine;

  public MinimumSpanningTreeDemo() {
    super(true, ManifoldDisplays.manifolds(), List.of(LogWeightings.DISTANCES));
    // ---
    spinnerRefine = SpinnerLabel.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    spinnerRefine.setValue(2);
    spinnerRefine.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "refinement");
    // ---
    Distribution distribution = UniformDistribution.of(-4, 4);
    setControlPointsSe2(RandomVariate.of(distribution, 20, 3));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    RenderQuality.setQuality(graphics);
    Tensor sequence = getGeodesicControlPoints();
    Tensor domain = Subdivide.of(0.0, 1.0, 10);
    final int splits = spinnerRefine.getValue();
    DisjointSets disjointSets = DisjointSets.allocate(sequence.length());
    if (0 < sequence.length()) {
      Tensor matrix = distanceMatrix(sequence);
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
    for (int index = 0; index < sequence.length(); ++index) {
      int unique = map.get(disjointSets.key(index));
      Color color = ColorDataLists._097.cyclic().getColor(unique);
      PointsRender pointsRender = new PointsRender(color, color);
      pointsRender.show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), Tensors.of(sequence.get(index))).render(geometricLayer, graphics);
    }
  }

  public Tensor distanceMatrix(Tensor sequence) {
    Sedarim sedarim = biinvariant().distances(sequence);
    Tensor matrix = Tensor.of(sequence.stream().map(sedarim::sunder));
    return SymmetricMatrixQ.of(matrix) //
        ? matrix
        : Symmetrize.of(matrix);
  }

  public static void main(String[] args) {
    new MinimumSpanningTreeDemo().setVisible(1000, 600);
  }
}
