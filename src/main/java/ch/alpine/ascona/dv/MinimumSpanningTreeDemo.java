// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairIndexed;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.MatrixPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.util.DisjointSets;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.fit.IntUndirectedEdge;
import ch.alpine.sophis.fit.MinimumSpanningTree;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.Symmetrize;
import ch.alpine.tensor.mat.SymmetricMatrixQ;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.sca.Abs;

class MinimumSpanningTreeDemo extends ControlPointsDemo {
  private record EdgeComparator(Tensor matrix) implements Comparator<IntUndirectedEdge> {
    @Override
    public int compare(IntUndirectedEdge edge1, IntUndirectedEdge edge2) {
      return Scalars.compare( //
          edge1.Get(matrix), //
          edge2.Get(matrix));
    }
  }

  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "10", "20", "30", "50" })
    public Integer size = 20;
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  static class Param1 {
    @FieldSelectionCallback("biinvariants")
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldClip(min = "1", max = "8")
    public Integer refine = 2;
    public ColorDataLists colorDataLists = ColorDataLists._097;
    public ColorDataGradients cdg = ColorDataGradients.TEMPERATURE_LIGHT;

    @ReflectionMarker
    public List<Biinvariants> biinvariants() {
      return Biinvariants.OKAY;
    }
  }

  private final Param0 param0;
  private final Param1 param1;

  public MinimumSpanningTreeDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    // ---
    fieldsEditor(param0).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.ADDREMOVE;
  }

  private void shuffle() {
    int n = param0.size;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::point2xya));
    setControlPointsSe2(tensor);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Manifold manifold = manifoldDisplay.manifold();
    Tensor sequence = getGeodesicControlPoints();
    Tensor domain = Subdivide.of(0.0, 1.0, 10);
    final int splits = param1.refine;
    DisjointSets disjointSets = DisjointSets.allocate(sequence.length());
    if (0 < sequence.length()) {
      Tensor matrix = StaticHelper.distanceMatrix(param1.biinvariants.ofSafe(manifold), sequence);
      Dimension dimension = geometricComponent().getSize();
      Show show = new Show();
      show.add(MatrixPlot.of(matrix, param1.cdg, false));
      {
        boolean isSymmetric = SymmetricMatrixQ.INSTANCE.test(matrix);
        if (!isSymmetric) {
          Tensor defect = SymmetricMatrixQ.INSTANCE.defect(matrix);
          Scalar optional = Flatten.scalars(defect).map(Abs.FUNCTION).reduce(Max::of).orElseThrow();
          show.setPlotLabel("not symmetric: " + optional);
        }
      }
      show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 0, 400, Math.min(dimension.height, 300)));
      {
        // TODO gives points of dimensions n x n-1
        // Tensor points = DistanceMatrixToPoints.of(Symmetrize.of(matrix), Chop._04);
        // IO.println(Dimensions.of(points));
      }
      List<IntUndirectedEdge> list = MinimumSpanningTree.of(Symmetrize.of(matrix));
      list.sort(new EdgeComparator(matrix));
      int count = Math.max(0, list.size() - splits);
      {
        for (IntUndirectedEdge directedEdge : list.subList(0, count))
          disjointSets.union(directedEdge.i(), directedEdge.j());
      }
      graphics.setColor(Color.DARK_GRAY);
      for (IntUndirectedEdge directedEdge : list.subList(0, count)) {
        Tensor p = sequence.get(directedEdge.i());
        Tensor q = sequence.get(directedEdge.j());
        ScalarTensorFunction curve = geodesicSpace.curve(p, q);
        Tensor tensor = Tensor.of(domain.maps(curve).stream().map(manifoldDisplay::point2xy));
        Path2D line = geometricLayer.toPath2D(tensor);
        graphics.draw(line);
      }
    }
    Map<Integer, Integer> map = disjointSets.createMap(new AtomicInteger()::getAndIncrement);
    ColorPairIndexed colorPairIndexed = new ColorPairIndexed(param1.colorDataLists.cyclic(), 128, 255);
    for (int index = 0; index < sequence.length(); ++index) {
      int unique = map.get(disjointSets.key(index));
      manifoldDisplay.showPoints(colorPairIndexed.getColorPair(unique), RealScalar.ONE, Tensors.of(sequence.get(index))) //
          .render(geometricLayer, graphics);
    }
  }

  static void main() {
    new MinimumSpanningTreeDemo().runStandalone();
  }
}
