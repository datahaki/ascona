// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.Container;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
class SpiralDemo implements ManipulateProvider, RenderInterface {
  public SpiralParam spiralParam = SpiralParam.EULER;
  public Clip clip = Clips.absolute(10);
  public Integer samples = 5000;
  private final GeometricComponent geometricComponent = new GeometricComponent();

  public SpiralDemo() {
    geometricComponent.addRenderInterfaceBackground(new GridRender(geometricComponent::getSize));
    geometricComponent.addRenderInterface(this);
    Tensor pvm = PvmBuilder.rhs().setOffset(300, 300).setPerPixel(150).digest();
    geometricComponent.setModel2Pixel(pvm);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    {
      Tensor points = Subdivide.increasing(clip, samples).maps(spiralParam.scalarTensorFunction);
      new PathRender(ColorStroke.CURVE, points, false).render(geometricLayer, graphics);
    }
    {
      Tensor points = Subdivide.increasing(clip, 50).maps(spiralParam.scalarTensorFunction);
      ManifoldDisplay manifoldDisplay = ManifoldDisplays.ClA.manifoldDisplay();
      manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.of(0.1), points) //
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
