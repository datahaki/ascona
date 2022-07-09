// code by jph
package ch.alpine.ascona.dv;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.List;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.cls.Classification;
import ch.alpine.ascona.util.cls.Labels;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.d.DiscreteUniformDistribution;
import ch.alpine.tensor.sca.pow.Sqrt;

public class ClassificationDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.manifolds());
      manifoldDisplays = ManifoldDisplays.Se2;
      drawControlPoints = false;
    }

    @FieldInteger
    @FieldSelectionArray({ "10", "20", "50" })
    public Scalar size = RealScalar.of(20);
    @FieldFuse("shuffle")
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param1 {
    @FieldSelectionCallback("biinvariants")
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public ColorDataLists cdg = ColorDataLists._097;
    public Boolean connect = true;
    public Boolean weights = true;
    public Labels labels = Labels.ARG_MIN;

    @ReflectionMarker
    public List<Biinvariants> biinvariants() {
      return Biinvariants.FAST;
    }
  }

  private final Param0 param0;
  private final Param1 param1;
  private Tensor sequence;
  // ---
  private Tensor vector;

  public ClassificationDemo() {
    this(new Param0(), new Param1());
  }

  public ClassificationDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}}"));
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  protected void shuffle() {
    // assignment of random labels to points
    int n = param0.size.number().intValue();
    sequence = RandomSample.of(manifoldDisplay().randomSampleInterface(), n);
    vector = RandomVariate.of(DiscreteUniformDistribution.of(0, 3), n);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Tensor origin = getGeodesicControlPoints().get(0);
    // ---
    Sedarim sedarim = LogWeightings.DISTANCES.sedarim(param1.biinvariants.ofSafe(manifold), null, sequence);
    Tensor weights = sedarim.sunder(origin);
    // leversRender.renderInfluenceX(ColorDataGradients.JET);
    // Tensor influence = new HsInfluence( //
    // geodesicDisplay.vectorLogManifold().logAt(leversRender.getOrigin()), //
    // control).matrix();
    // List<Edge> list = PrimAlgorithm.of(influence);
    // graphics.setColor(Color.BLACK);
    // Tensor domain = Subdivide.of(0.0, 1.0, 10);
    // for (Edge edge : list) {
    // Tensor p = control.get(edge.i);
    // Tensor q = control.get(edge.j);
    // ScalarTensorFunction curve = geodesicInterface.curve(p, q);
    // Path2D line = geometricLayer.toPath2D(domain.map(curve));
    // graphics.draw(line);
    // }
    LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
    if (param1.connect)
      leversRender.renderLevers(param1.labels.equals(Labels.ARG_MIN) //
          ? Sqrt.of(weights).negate()
          : weights);
    if (param1.weights)
      leversRender.renderWeights(weights);
    // ---
    ColorDataIndexed COLOR_DATA_INDEXED_O = param1.cdg.cyclic();
    ColorDataIndexed COLOR_DATA_INDEXED_T = COLOR_DATA_INDEXED_O.deriveWithAlpha(128);
    Tensor shape = manifoldDisplay.shape();
    int index = 0;
    for (Tensor point : sequence) {
      int label = vector.Get(index).number().intValue();
      PointsRender pointsRender = new PointsRender( //
          COLOR_DATA_INDEXED_T.getColor(label), //
          COLOR_DATA_INDEXED_O.getColor(label));
      pointsRender.show(manifoldDisplay::matrixLift, shape, Tensors.of(point)).render(geometricLayer, graphics);
      ++index;
    }
    // ---
    Classification classification = param1.labels.apply(vector);
    int bestLabel = classification.result(weights).getLabel();
    geometricLayer.pushMatrix(manifoldDisplay.matrixLift(origin));
    Path2D path2d = geometricLayer.toPath2D(shape.multiply(RealScalar.of(1.4)), true);
    graphics.setColor(COLOR_DATA_INDEXED_O.getColor(bestLabel));
    graphics.fill(path2d);
    // ---
    graphics.setStroke(new BasicStroke(1.5f));
    graphics.draw(path2d);
    geometricLayer.popMatrix();
  }

  public static void main(String[] args) {
    launch();
  }
}
