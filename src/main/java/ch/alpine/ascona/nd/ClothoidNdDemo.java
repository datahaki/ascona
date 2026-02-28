// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
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
import ch.alpine.tensor.opt.nd.NdCenterInterface;
import ch.alpine.tensor.opt.nd.NdCenters;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.sca.Clips;

class ClothoidNdDemo extends ControlPointsDemo {
  private static final int SIZE = 400;
  private static final CoordinateBoundingBox ND_BOX_R2 = //
      CoordinateBoundingBox.of(Clips.absolute(5), Clips.absolute(5));
  private static final CoordinateBoundingBox ND_BOX_SE2 = //
      CoordinateBoundingBox.of(Clips.absolute(5), Clips.absolute(5), Clips.absolute(Math.PI));
  // ---
  private final ClothoidNdMap<Tensor> clothoidNdMap = new ClothoidNdMap<>(ND_BOX_R2, t -> t);

  @ReflectionMarker
  static class Param {
    @FieldClip(min = "1", max = "30")
    @FieldSlider
    public Integer value = 8;
  }

  private final Param param;

  public ClothoidNdDemo() {
    super(param = new Param());
    // ---
    RandomSampleInterface randomSampleInterface = new BoxRandomSample(ND_BOX_SE2);
    Tensor tensor = RandomSample.of(randomSampleInterface, SIZE);
    for (Tensor state : tensor)
      clothoidNdMap.insert(state);
    setControlPointsSe2(tensor);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.CL_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    int _value = param.value;
    Tensor mouse = geometricComponent().getMouseSe2CState();
    {
      NdCenterInterface ndCenterInterface = NdCenters.VECTOR_2_NORM.apply(mouse.extract(0, 2));
      GraphicNearest<Tensor> graphicNearest = //
          new GraphicNearest<>(ndCenterInterface, _value, geometricLayer, graphics);
      clothoidNdMap.ndMap.visit(graphicNearest);
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), mouse, geometricLayer, graphics);
    leversRender.renderSequence();
    leversRender.renderOrigin();
    // ---
    ClothoidBuilder clothoidBuilder = (ClothoidBuilder) manifoldDisplay.geodesicSpace();
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
