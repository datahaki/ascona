// code by jph
package ch.alpine.ascona.lev;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;

import javax.swing.JButton;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
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

public class KNearestDemo extends LogWeightingDemo {
  @ReflectionMarker
  public static class Param {
    @FieldInteger
    @FieldClip(min = "9", max = "75")
    public Scalar length = RealScalar.of(9);
    public Tensor tensor = Tensors.vector(.3, 0, .6);
  }

  private final Param param = new Param();
  private final JButton jButton = new JButton("shuffle");

  public KNearestDemo() {
    super(true, ManifoldDisplays.SE2_ONLY, LogWeightings.list());
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    jButton.addActionListener(l -> shuffleSnap());
    timerFrame.jToolBar.add(jButton);
    setManifoldDisplay(ManifoldDisplays.Se2);
    setLogWeighting(LogWeightings.DISTANCES);
    shuffleSnap();
  }

  private void shuffleSnap() {
    // Distribution distributionP = NormalDistribution.standard();
    Distribution distributionA = UniformDistribution.of(Clips.absolute(Pi.VALUE));
    Tensor sequence = RandomVariate.of(distributionA, param.length.number().intValue(), 3);
    // sequence.set(s -> RandomVariate.of(distributionA), Tensor.ALL, 2);
    sequence.set(Scalar::zero, 0, Tensor.ALL);
    setControlPointsSe2(sequence);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Optional<Tensor> optional = getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = getSequence();
      Tensor origin = optional.get();
      // ---
      render(geometricLayer, graphics, sequence, origin, "");
      LieGroupOps lieGroupOps = new LieGroupOps(Se2Group.INSTANCE);
      try {
        Tensor shift = param.tensor;
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
    Sedarim sedarim = logWeighting().sedarim(biinvariant(), variogram(), sequence);
    Tensor weights = sedarim.sunder(origin);
    // ---
    int[] integers = Ordering.INCREASING.of(weights);
    Tensor shape = manifoldDisplay.shape();
    for (int index = 0; index < sequence.length(); ++index) {
      Tensor point = sequence.get(integers[index]);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
      Path2D path2d = geometricLayer.toPath2D(shape, true);
      graphics.setColor(index < 3 ? new Color(64, 192, 64, 64) : new Color(192, 64, 64, 64));
      graphics.fill(path2d);
      graphics.setColor(index < 3 ? new Color(64, 192, 64, 255) : new Color(192, 64, 64, 255));
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
