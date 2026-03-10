// code by jph
package ch.alpine.ascona.usr;

import java.awt.Container;

import ch.alpine.ascony.reg.BallRegionRender;
import ch.alpine.ascony.reg.ConeRegionRender;
import ch.alpine.ascony.reg.PolygonRegionRender;
import ch.alpine.ascony.reg.RegionRenders;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.HilbertPolygon;
import ch.alpine.sophis.reg.BallRegion;
import ch.alpine.sophis.reg.ConeRegion;
import ch.alpine.sophis.reg.EllipsoidRegion;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

@ReflectionMarker
public class RegionRenderDemo implements ManipulateProvider {
  private final GeometricComponent geometricComponent = new GeometricComponent();

  public RegionRenderDemo() {
    Tensor digest = PvmBuilder.rhs().setOffset(100, 600).setPerPixel(Quantity.of(60, "m^-1")).digest();
    geometricComponent.setModel2Pixel(digest);
    // geometricComponent.setPerPixel(Quantity.of(60, "m^-1"));
    {
      BallRegion ballRegion = new BallRegion(Tensors.fromString("{2[m],3[m]}"), Quantity.of(1, "m"));
      BallRegionRender ballRegionRender = new BallRegionRender(ballRegion);
      geometricComponent.addRenderInterface(ballRegionRender);
    }
    {
      ConeRegion coneRegion = new ConeRegion(Tensors.fromString("{-2[m],3[m],1.3}"), RealScalar.ONE);
      ConeRegionRender coneRegionRender = new ConeRegionRender(coneRegion);
      geometricComponent.addRenderInterface(coneRegionRender);
    }
    {
      EllipsoidRegion ellipsoidRegion = //
          new EllipsoidRegion(Tensors.fromString("{2[m],-1[m]}"), Tensors.fromString("{1[m],0.5[m]}"));
      RenderInterface renderInterface = RegionRenders.of(ellipsoidRegion);
      geometricComponent.addRenderInterface(renderInterface);
    }
    {
      RenderInterface renderInterface = RegionRenders.of(CoordinateBoundingBox.of( //
          Clips.interval(Quantity.of(-4, "m"), Quantity.of(-3, "m")), //
          Clips.absolute(Quantity.of(1, "m"))));
      geometricComponent.addRenderInterface(renderInterface);
    }
    {
      Tensor polygon = HilbertPolygon.of(3).multiply(Quantity.of(0.1, "m"));
      geometricComponent.addRenderInterface(new PolygonRegionRender(polygon));
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
    new RegionRenderDemo().runStandalone();
  }
}
