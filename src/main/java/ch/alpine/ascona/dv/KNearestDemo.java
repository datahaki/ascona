// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Optional;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.ascony.win.PlaceWrap;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.sophus.math.Permute;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Clips;

class KNearestDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldFuse
    public transient Boolean shuffle;
    @FieldClip(min = "4", max = "10")
    public Integer length = 8;
  }

  @ReflectionMarker
  static class Param1 {
    public Biinvariants biinvariants = Biinvariants.USANCE;
    public Tensor tensor = Tensors.vector(.3, 0, .6);
    @FieldClip(min = "2", max = "5")
    public Integer k = 3;
  }

  private final Param0 param0;
  private final Param1 param1;

  public KNearestDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(param0).addUniversalListener(this::shuffleSnap);
    shuffleSnap();
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.ADDREMOVE;
  }

  private void shuffleSnap() {
    Distribution distributionA = UniformDistribution.of(Clips.absolute(Pi.VALUE));
    Tensor sequence = RandomVariate.of(distributionA, param0.length, 3);
    sequence.set(Scalar::zero, 0, Tensor.ALL);
    setControlPointsSe2(sequence);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      // ---
      render(geometricLayer, graphics, sequence, origin, "");
      LieGroup lieGroup = Se2Group.INSTANCE;
      try {
        Tensor shift = param1.tensor;
        {
          geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(8, 0)));
          TensorUnaryOperator lieGroupOp = lieGroup.conjugation(shift);
          render(geometricLayer, graphics, Tensor.of(sequence.stream().map(lieGroupOp)), lieGroupOp.apply(origin), "'");
          geometricLayer.popMatrix();
        }
        {
          Tensor invert = Se2Group.INSTANCE.invert(shift);
          geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(16, 0)));
          TensorUnaryOperator lieGroupOp = lieGroup.conjugation(invert);
          render(geometricLayer, graphics, Tensor.of(sequence.stream().map(lieGroupOp)), lieGroupOp.apply(origin), "\"");
          geometricLayer.popMatrix();
        }
      } catch (Exception exception) {
        System.err.println(exception);
      }
    }
  }

  public void render(GeometricLayer geometricLayer, Graphics2D graphics, Tensor sequence, Tensor origin, String p) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = manifoldDisplay.manifold();
    Sedarim sedarim = LogWeightings.DISTANCES.sedarim(param1.biinvariants.ofSafe(manifold), s -> s, sequence);
    Tensor weights = sedarim.sunder(origin);
    // ---
    int[] integers = Ordering.INCREASING.of(weights);
    Tensor seq = Permute.of(integers).apply(sequence);
    int k = param1.k;
    manifoldDisplay.showPoints(ColorPair.GROUP_NEAR, RealScalar.ONE, seq.extract(0, k)) //
        .render(geometricLayer, graphics);
    manifoldDisplay.showPoints(ColorPair.GROUP_AFAR, RealScalar.ONE, seq.extract(k, sequence.length())) //
        .render(geometricLayer, graphics);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
    leversRender.renderOrigin();
    leversRender.renderIndexX("x" + p);
    leversRender.renderIndexP("p" + p);
  }

  static void main() {
    new KNearestDemo().runStandalone();
  }
}
