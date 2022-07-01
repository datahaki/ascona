// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.crv.Transition;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidTransition;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

public class ClothoidNdDemo extends ControlPointsDemo {
  private static final int SIZE = 400;
  private static final CoordinateBoundingBox ND_BOX_R2 = CoordinateBoundingBox.of( //
      Clips.absolute(5), Clips.absolute(5));
  private static final CoordinateBoundingBox ND_BOX_SE2 = CoordinateBoundingBox.of( //
      Clips.absolute(5), Clips.absolute(5), Clips.absolute(Math.PI));
  // ---
  private final ClothoidNdMap<Tensor> clothoidNdMap = new ClothoidNdMap<>(ND_BOX_R2, t -> t);

  @ReflectionMarker
  public static class Param {
    @FieldInteger
    @FieldClip(min = "1", max = "20")
    @FieldSlider
    public Scalar value = RealScalar.of(3);
  }

  private final Param param = new Param();

  public ClothoidNdDemo() {
    super(false, ManifoldDisplays.CL_ONLY);
    // ---
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    // ---
    renderInterface.setPositioningEnabled(false);
    // ---
    RandomSampleInterface randomSampleInterface = BoxRandomSample.of(ND_BOX_SE2);
    Tensor tensor = RandomSample.of(randomSampleInterface, SIZE);
    for (Tensor state : tensor)
      clothoidNdMap.insert(state);
    setControlPointsSe2(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), mouse, geometricLayer, graphics);
    leversRender.renderSequence();
    leversRender.renderOrigin();
    // ---
    ClothoidBuilder clothoidBuilder = (ClothoidBuilder) manifoldDisplay.geodesicSpace();
    int _value = Scalars.intValueExact(param.value);
    graphics.setColor(new Color(255, 0, 0, 128));
    Scalar minResolution = RealScalar.of(geometricLayer.pixel2modelWidth(10));
    for (Clothoid clothoid : clothoidNdMap.cl_nearFrom(clothoidBuilder, mouse, _value)) {
      Transition transition = ClothoidTransition.of(clothoid);
      graphics.draw(geometricLayer.toPath2D(transition.linearized(minResolution)));
    }
    // ---
    graphics.setColor(new Color(0, 255, 0, 128));
    for (Clothoid clothoid : clothoidNdMap.cl_nearTo(clothoidBuilder, mouse, _value)) {
      Transition transition = ClothoidTransition.of(clothoid);
      graphics.draw(geometricLayer.toPath2D(transition.linearized(minResolution)));
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new ClothoidNdDemo().setVisible(1200, 800);
  }
}
