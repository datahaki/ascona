// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.Se2ClothoidDisplay;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.Arrowhead;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
class SpiralDemo implements ManipulateProvider, RenderInterface {
  private static final PointsRender POINTS_RENDER = //
      new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 128));
  public SpiralParam spiralParam = SpiralParam.EULER;
  public Clip clip = Clips.absolute(10);
  public Integer samples = 5000;
  private final GeometricComponent geometricComponent = new GeometricComponent();

  public SpiralDemo() {
    geometricComponent.addRenderInterfaceBackground(new GridRender(geometricComponent::getSize));
    geometricComponent.addRenderInterface(this);
    geometricComponent.setPerPixel(RealScalar.of(100));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    {
      Tensor points = Subdivide.increasing(clip, samples).maps(spiralParam.scalarTensorFunction);
      new PathRender(Color.BLUE, 1f).setCurve(points, false).render(geometricLayer, graphics);
    }
    {
      Tensor points = Subdivide.increasing(clip, 50).maps(spiralParam.scalarTensorFunction);
      POINTS_RENDER.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.03), points) //
          .render(geometricLayer, graphics);
    }
    graphics.drawString(spiralParam.scalarTensorFunction.toString(), 100, 50);
  }

  @Override
  public Container getContainer() {
    return geometricComponent;
  }

  static void main() {
    new SpiralDemo().runStandalone();
  }
}
