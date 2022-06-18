// code by jph
package ch.alpine.ascona.dv;

import java.awt.Dimension;
import java.awt.Graphics2D;

import ch.alpine.ascona.lev.AbstractHoverDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradients;

public class OrderingHoverDemo extends AbstractHoverDemo {
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);

  public OrderingHoverDemo() {
    super(200);
    {
      spinnerColorData.setValue(ColorDataGradients.THERMOMETER);
      spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(200, 28), "color");
    }
    setLogWeighting(LogWeightings.DISTANCES);
  }

  @Override // from AbstractHoverDemo
  protected void render(GeometricLayer geometricLayer, Graphics2D graphics, LeversRender leversRender) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    Sedarim tensorUnaryOperator = //
        logWeighting().operator(biinvariant(), variogram(), getGeodesicControlPoints());
    RenderQuality.setQuality(graphics);
    Tensor sequence = leversRender.getSequence();
    Tensor origin = leversRender.getOrigin();
    Tensor weights = tensorUnaryOperator.sunder(origin);
    // ---
    OrderingHelper.of(manifoldDisplay, origin, sequence, weights, spinnerColorData.getValue(), geometricLayer, graphics);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new OrderingHoverDemo().setVisible(1200, 600);
  }
}
