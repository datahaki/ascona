// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataGradients;

public class OrderingHoverDemo extends ControlPointsDemo {
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.manifolds());
    }

    @FieldInteger
    @FieldSelectionArray({ "10", "50", "100", "200" })
    public Scalar size = RealScalar.of(200);
    @FieldFuse("shuffle")
    public Boolean shuffle;
  }

  public static class Param1 {
    public LogWeightings logWeightings = LogWeightings.DISTANCES;
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public ColorDataGradients cdg = ColorDataGradients.THERMOMETER;
  }

  private final Param0 param0;
  private final Param1 param1;
  private Tensor points;

  public OrderingHoverDemo() {
    this(new Param0(), new Param1());
  }

  public OrderingHoverDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}}"));
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    points = RandomSample.of(manifoldDisplay().randomSampleInterface(), param0.size.number().intValue());
  }

  @Override // from AbstractHoverDemo
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor origin = getGeodesicControlPoints().get(0);
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Sedarim sedarim = param1.logWeightings.sedarim(param1.biinvariants.of(manifold), InversePowerVariogram.of(2), points);
    RenderQuality.setQuality(graphics);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, points, origin, geometricLayer, graphics);
    Tensor sequence = leversRender.getSequence();
    Tensor weights = sedarim.sunder(origin);
    // ---
    OrderingHelper.of(manifoldDisplay, origin, sequence, weights, param1.cdg, geometricLayer, graphics);
  }

  public static void main(String[] args) {
    launch();
  }
}
