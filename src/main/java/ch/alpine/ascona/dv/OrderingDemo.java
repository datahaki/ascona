// code by jph
package ch.alpine.ascona.dv;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.BarLegend;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorFormat;

public class OrderingDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.manifolds());
      manifoldDisplays = ManifoldDisplays.Se2;
      drawControlPoints = false;
    }

    @FieldInteger
    @FieldSelectionArray({ "10", "50", "100" })
    public Scalar size = RealScalar.of(100);
    @FieldFuse("shuffle")
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param1 {
    @FieldSelectionCallback("biinvariants")
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public ColorDataGradients cdg = ColorDataGradients.THERMOMETER;
    @FieldInteger
    @FieldSelectionArray({ "1", "3", "5", "8" })
    public Scalar closest = RealScalar.of(8);

    @ReflectionMarker
    public List<Biinvariants> biinvariants() {
      return Biinvariants.FAST;
    }
  }

  private final Param0 param0;
  private final Param1 param1;
  private Tensor sequence;

  public OrderingDemo() {
    this(new Param0(), new Param1());
  }

  public OrderingDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}}"));
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    sequence = RandomSample.of(manifoldDisplay().randomSampleInterface(), param0.size.number().intValue());
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor origin = getGeodesicControlPoints().get(0);
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    // ---
    Tensor weights = LogWeightings.DISTANCES.sedarim(param1.biinvariants.ofSafe(manifold), null, sequence) //
        .sunder(origin);
    // ---
    int[] integers = Ordering.INCREASING.of(weights);
    ColorDataGradient colorDataGradientF = param1.cdg.deriveWithOpacity(RationalScalar.HALF);
    ColorDataGradient colorDataGradientD = param1.cdg;
    RenderQuality.setQuality(graphics);
    Tensor shape = manifoldDisplay.shape();
    for (int index = 0; index < sequence.length(); ++index) {
      Tensor point = sequence.get(integers[index]);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
      Path2D path2d = geometricLayer.toPath2D(shape, true);
      Scalar ratio = RationalScalar.of(index, integers.length);
      graphics.setColor(ColorFormat.toColor(colorDataGradientF.apply(ratio)));
      graphics.fill(path2d);
      graphics.setColor(ColorFormat.toColor(colorDataGradientD.apply(ratio)));
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    {
      BufferedImage bufferedImage = BarLegend.of(colorDataGradientD, 200, "far", "near");
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      graphics.drawImage(bufferedImage, dimension.width - bufferedImage.getWidth(), 0, null);
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, //
          Tensor.of(IntStream.range(0, param1.closest.number().intValue()).limit(integers.length) //
              .map(index -> integers[index]) //
              .mapToObj(sequence::get)), //
          origin, geometricLayer, graphics);
      leversRender.renderLevers();
      leversRender.renderOrigin();
      leversRender.renderIndexX();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
