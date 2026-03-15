// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
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
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "5", "10", "20", "30" })
    public Integer numel = 30;
    public transient Boolean shuffle = true;
  }

  private final Param param;

  public PrincipalComponentsDemo() {
    super(param = new Param());
    fieldsEditor(param).addUniversalListener(this::shuffle);
    geometricComponent().addRenderInterfaceBackground(AxesRender.INSTANCE);
    shuffle();
  }

  void shuffle() {
    SpdNManifold spdNManifold = new SpdNManifold(2);
    Tensor p = RandomSample.of(spdNManifold);
    p = Tensors.fromString("{{2,2},{2,1}}");
    RandomSampleInterface rsi = MultinormalDistribution.of(p);
    Tensor t = Tensors.vector(4, 0);
    Tensor points = RandomSample.of(rsi, param.numel);
    TensorUnaryOperator tuo = t::add;
    setGeodesicControlPoints(tuo.slash(points));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor points = getGeodesicControlPoints();
    PrincipalComponents pc = PrincipalComponents.of(points);
    Tensor tensor = pc.unscaled();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    manifoldDisplay.showPoints(ColorPair.REFERENCE, RealScalar.of(0.8), tensor) //
        .render(geometricLayer, graphics);
    Tensor slash = pc.scaled_directions();
    graphics.setColor(Color.BLUE);
    Tensor mean = pc.mean();
    graphics.draw(geometricLayer.toLine2D(mean, mean.add(slash.get(0))));
    graphics.draw(geometricLayer.toLine2D(mean, mean.add(slash.get(1))));
  }

  static void main() {
    new PrincipalComponentsDemo().runStandalone();
  }
}
