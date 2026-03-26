// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.ref.BiinvariantsParam;
import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.cls.Classification;
import ch.alpine.ascony.cls.Labels;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairIndexed;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.d.DiscreteUniformDistribution;
import ch.alpine.tensor.sca.pow.Sqrt;

class ClassificationDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "10", "20", "50" })
    public Integer size = 20;
    public final ShuffleFuse shuffleFuse = new ShuffleFuse();
  }

  @ReflectionMarker
  static class Param1 {
    public final BiinvariantsParam biinvariantsParam = BiinvariantsParam.okay();
    public ColorDataLists cdg = ColorDataLists._097;
    public Boolean connect = true;
    public Boolean weights = true;
    public Labels labels = Labels.ARG_MIN;
  }

  private final Param0 param0;
  private final Param1 param1;
  // ---
  private Tensor sequence;
  private Tensor vector;

  public ClassificationDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(param0).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    shuffle();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.DELEGATED;
  }

  protected void shuffle() {
    // assignment of random labels to points
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}}"));
    int n = param0.size;
    sequence = RandomSample.of(manifoldDisplay().randomSampleInterface(), n);
    vector = RandomVariate.of(DiscreteUniformDistribution.forArray(3), n);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor origin = getGeodesicControlPoints().get(0);
    Manifold manifold = manifoldDisplay.manifold();
    // ---
    Sedarim sedarim = LogWeightings.DISTANCES.sedarim(param1.biinvariantsParam.ofSafe(manifold), null, sequence);
    Tensor weights = sedarim.sunder(origin);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
    if (param1.connect)
      leversRender.renderLevers(param1.labels.equals(Labels.ARG_MIN) //
          ? weights.maps(Sqrt.FUNCTION).negate()
          : weights);
    if (param1.weights)
      leversRender.renderWeights(weights);
    // ---
    ColorPairIndexed colorPairIndexed = new ColorPairIndexed(param1.cdg.cyclic(), 128, 255);
    int index = 0;
    for (Tensor point : sequence) {
      int label = Scalars.intValueExact(vector.Get(index));
      manifoldDisplay.showPoints(colorPairIndexed.getColorPair(label), RealScalar.ONE, Tensors.of(point)) //
          .render(geometricLayer, graphics);
      ++index;
    }
    // ---
    Classification classification = param1.labels.apply(vector);
    int bestLabel = classification.result(weights).label();
    manifoldDisplay.showPoints(colorPairIndexed.getColorPair(bestLabel).solid(), RealScalar.of(1.2), Tensors.of(origin)) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    new ClassificationDemo().runStandalone();
  }
}
