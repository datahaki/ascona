// code by jph
package ch.alpine.ascona.nd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JScrollPane;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.PanelFieldsEditor;
import ch.alpine.sophus.api.Transition;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidTransition;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;

public class ClothoidNdDemo extends ControlPointsDemo {
  private static final int SIZE = 400;
  private static final CoordinateBoundingBox ND_BOX_R2 = CoordinateBounds.of( //
      Tensors.vector(-5, -5), //
      Tensors.vector(+5, +5));
  private static final CoordinateBoundingBox ND_BOX_SE2 = CoordinateBounds.of( //
      Tensors.vector(-5, -5, -Math.PI), //
      Tensors.vector(+5, +5, +Math.PI));
  // ---
  private final Se2NdMap<Tensor> se2NdMap = new Se2NdMap<>(ND_BOX_R2, t -> t);
  // private final RrtsNodeCollection rrtsNodeCollection1 = //
  // Se2RrtsNodeCollections.of(ClothoidTransitionSpace.ANALYTIC, ND_BOX_SE2);
  // private final RrtsNodeCollection rrtsNodeCollection2 = //
  // ClothoidRrtsNodeCollections.of(RealScalar.ONE, ND_BOX_SE2);

  // ---
  @ReflectionMarker
  public static class Param {
    public Boolean limit = true;
    @FieldInteger
    @FieldClip(min = "1", max = "50")
    public Scalar value = RealScalar.of(3);
  }

  public final Param param = new Param();

  public ClothoidNdDemo() {
    super(false, ManifoldDisplays.CL_ONLY);
    // ---
    Container container = timerFrame.jFrame.getContentPane();
    JScrollPane jScrollPane = new PanelFieldsEditor(param).createJScrollPane();
    jScrollPane.setPreferredSize(new Dimension(120, 200));
    container.add(BorderLayout.WEST, jScrollPane);
    // ---
    setPositioningEnabled(false);
    setMidpointIndicated(false);
    // ---
    RandomSampleInterface randomSampleInterface = BoxRandomSample.of(ND_BOX_SE2);
    Tensor tensor = RandomSample.of(randomSampleInterface, SIZE);
    for (Tensor state : tensor) {
      se2NdMap.insert(state);
    }
    setControlPointsSe2(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), mouse, geometricLayer, graphics);
    leversRender.renderSequence();
    leversRender.renderOrigin();
    // ---
    // RrtsNodeCollection rrtsNodeCollection = param.limit //
    // ? rrtsNodeCollection2
    // : rrtsNodeCollection1;
    int _value = Scalars.intValueExact(param.value);
    graphics.setColor(new Color(255, 0, 0, 128));
    Scalar minResolution = RealScalar.of(geometricLayer.pixel2modelWidth(10));
    for (Clothoid clothoid : se2NdMap.cl_nearFrom(mouse, _value)) {
      Transition transition = ClothoidTransition.of(clothoid);
      graphics.draw(geometricLayer.toPath2D(transition.linearized(minResolution)));
    }
    // ---
    graphics.setColor(new Color(0, 255, 0, 128));
    for (Clothoid clothoid : se2NdMap.cl_nearTo(mouse, _value)) {
      Transition transition = ClothoidTransition.of(clothoid);
      graphics.draw(geometricLayer.toPath2D(transition.linearized(minResolution)));
    }
  }

  public static void main(String[] args) {
    new ClothoidNdDemo().setVisible(1200, 800);
  }
}
