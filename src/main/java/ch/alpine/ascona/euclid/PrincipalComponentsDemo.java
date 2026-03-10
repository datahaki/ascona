// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.spd.SpdNManifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.mat.cd.MultinormalDistribution;
import ch.alpine.tensor.mat.sv.PrincipalComponents;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

class PrincipalComponentsDemo extends EuclideanPlaneDemo {
  public PrincipalComponentsDemo() {
    SpdNManifold spdNManifold = new SpdNManifold(2);
    Tensor p = RandomSample.of(spdNManifold);
    RandomSampleInterface rsi = MultinormalDistribution.of(p);
    Tensor t = Tensors.vector(2, 0);
    Tensor points = RandomSample.of(rsi, 30);
    TensorUnaryOperator tuo = t::add;
    setGeodesicControlPoints(tuo.slash(points));
    geometricComponent().addRenderInterfaceBackground(AxesRender.INSTANCE);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor points = getGeodesicControlPoints();
    Tensor tensor = PrincipalComponents.of(points);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    manifoldDisplay.showPoints(Color.LIGHT_GRAY, Color.GRAY, RealScalar.of(0.6), tensor) //
        .render(geometricLayer, graphics);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.SCATTERED;
  }

  static void main() {
    new PrincipalComponentsDemo().runStandalone();
  }
}
