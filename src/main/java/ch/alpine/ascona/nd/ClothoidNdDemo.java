// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.sophis.ts.Transition;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.BoxRandomSample;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
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
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.CL_ONLY);
    }

    @FieldClip(min = "1", max = "20")
    @FieldSlider
    public Integer value = 3;
  }

  private final Param param;

  public ClothoidNdDemo() {
    this(new Param());
  }

  public ClothoidNdDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    controlPointsRender.setPositioningEnabled(false);
    // ---
    RandomSampleInterface randomSampleInterface = new BoxRandomSample(ND_BOX_SE2);
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
    int _value = param.value;
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

  static void main() {
    new ClothoidNdDemo().runStandalone();
  }
}
