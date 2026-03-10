// code by jph
package ch.alpine.ascona.usr;

import java.awt.Container;

import ch.alpine.ascony.reg.RegionRenders;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.reg.EllipsoidRegion;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

@ReflectionMarker
class MixedRenderDemo implements ManipulateProvider {
  private final GeometricComponent geometricComponent = new GeometricComponent();

  public MixedRenderDemo() {
    Tensor digest = PvmBuilder.rhs().setOffset(300, 600).setPerPixel(Quantity.of(60, "m^-1"), Quantity.of(30, "s^-1")).digest();
    geometricComponent.setModel2Pixel(digest);
    {
      EllipsoidRegion ellipsoidRegion = //
          new EllipsoidRegion(Tensors.fromString("{2[m],-1[s]}"), Tensors.fromString("{1[m],0.5[s]}"));
      RenderInterface renderInterface = RegionRenders.of(ellipsoidRegion);
      geometricComponent.addRenderInterface(renderInterface);
    }
    {
      RenderInterface renderInterface = RegionRenders.of(CoordinateBoundingBox.of( //
          Clips.interval(Quantity.of(-4, "m"), Quantity.of(-3, "m")), //
          Clips.absolute(Quantity.of(1, "s"))));
      geometricComponent.addRenderInterface(renderInterface);
    }
    {
      GridRender gridRender = new GridRender(geometricComponent::getSize);
      geometricComponent.addRenderInterfaceBackground(gridRender);
    }
    geometricComponent.addRenderInterfaceBackground(new MatRender(g -> g.getMatrix().maps(Round._1)));
  }

  @Override
  public Container getContainer() {
    return geometricComponent;
  }

  static void main() {
    new MixedRenderDemo().runStandalone();
  }
}
