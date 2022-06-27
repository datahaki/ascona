// code by jph
package ch.alpine.ascona.lev;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JButton;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.Tensor;

public abstract class AbstractHoverDemo extends LogWeightingDemo {
  public final SpinnerLabel<Integer> spinnerCount;
  private final JButton jButtonShuffle = new JButton("shuffle");

  public AbstractHoverDemo(int n) {
    super(false, ManifoldDisplays.manifolds(), LogWeightings.list());
    setPositioningEnabled(false);
    {
      spinnerCount = SpinnerLabel.of(5, 10, 15, 20, 25, 30, 40, 100, 200);
      spinnerCount.setValue(n);
      addManifoldListener(v -> shuffle());
      spinnerCount.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "magnify");
    }
    {
      jButtonShuffle.addActionListener(e -> shuffle());
      timerFrame.jToolBar.add(jButtonShuffle);
    }
    setManifoldDisplay(ManifoldDisplays.Se2);
    shuffle();
    timerFrame.jToolBar.addSeparator();
  }

  protected void shuffle() {
    System.out.println("shuffle");
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), spinnerCount.getValue()).stream() //
        .map(manifoldDisplay::point2xya));
    setControlPointsSe2(tensor);
  }

  @Override // from RenderInterface
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    Tensor origin = manifoldDisplay.xya2point(mouse);
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
    render(geometricLayer, graphics, leversRender);
  }

  /** @param geometricLayer
   * @param graphics
   * @param leversRender
   * @param weights */
  protected abstract void render(GeometricLayer geometricLayer, Graphics2D graphics, LeversRender leversRender);
}
