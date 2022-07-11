// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;

import ch.alpine.ascona.lev.PlaceWrap;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.lie.LieGroupOps;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.sophus.math.api.TensorMapping;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Clips;

public class KNearestDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.SE2_ONLY);
      drawControlPoints = false;
    }

    @FieldFuse
    public transient Boolean shuffle;
    @FieldInteger
    @FieldClip(min = "4", max = "10")
    public Scalar length = RealScalar.of(8);
  }

  @ReflectionMarker
  public static class Param1 {
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public Tensor tensor = Tensors.vector(.3, 0, .6);
    @FieldInteger
    @FieldClip(min = "2", max = "5")
    public Scalar k = RealScalar.of(3);
  }

  private final Param0 param0;
  private final Param1 param1;

  public KNearestDemo() {
    this(new Param0(), new Param1());
  }

  public KNearestDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    controlPointsRender.setMidpointIndicated(false);
    fieldsEditor(0).addUniversalListener(this::shuffleSnap);
    shuffleSnap();
  }

  private void shuffleSnap() {
    Distribution distributionA = UniformDistribution.of(Clips.absolute(Pi.VALUE));
    Tensor sequence = RandomVariate.of(distributionA, param0.length.number().intValue(), 3);
    sequence.set(Scalar::zero, 0, Tensor.ALL);
    setControlPointsSe2(sequence);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      // ---
      render(geometricLayer, graphics, sequence, origin, "");
      LieGroupOps lieGroupOps = new LieGroupOps(Se2Group.INSTANCE);
      try {
        Tensor shift = param1.tensor;
        {
          geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.vector(8, 0)));
          TensorMapping lieGroupOp = lieGroupOps.conjugation(shift);
          render(geometricLayer, graphics, lieGroupOp.slash(sequence), lieGroupOp.apply(origin), "'");
          geometricLayer.popMatrix();
        }
        {
          Tensor invert = lieGroupOps.inversion().apply(shift);
          geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.vector(16, 0)));
          TensorMapping lieGroupOp = lieGroupOps.conjugation(invert);
          render(geometricLayer, graphics, lieGroupOp.slash(sequence), lieGroupOp.apply(origin), "\"");
          geometricLayer.popMatrix();
        }
      } catch (Exception exception) {
        System.err.println(exception);
      }
    }
  }

  public void render(GeometricLayer geometricLayer, Graphics2D graphics, Tensor sequence, Tensor origin, String p) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Sedarim sedarim = LogWeightings.DISTANCES.sedarim(param1.biinvariants.ofSafe(manifold), s -> s, sequence);
    Tensor weights = sedarim.sunder(origin);
    // ---
    int[] integers = Ordering.INCREASING.of(weights);
    Tensor shape = manifoldDisplay.shape();
    int k = param1.k.number().intValue();
    for (int index = 0; index < sequence.length(); ++index) {
      Tensor point = sequence.get(integers[index]);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
      Path2D path2d = geometricLayer.toPath2D(shape, true);
      graphics.setColor(index < k ? new Color(64, 192, 64, 64) : new Color(192, 64, 64, 64));
      graphics.fill(path2d);
      graphics.setColor(index < k ? new Color(64, 192, 64, 255) : new Color(192, 64, 64, 255));
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    {
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(origin));
      Path2D path2d = geometricLayer.toPath2D(shape, true);
      graphics.setColor(Color.DARK_GRAY);
      graphics.fill(path2d);
      graphics.setColor(Color.BLACK);
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
    leversRender.renderIndexX("x" + p);
    leversRender.renderIndexP("p" + p);
  }

  public static void main(String[] args) {
    launch();
  }
}
