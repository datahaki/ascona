// code by jph
package ch.alpine.ascona.lev;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JButton;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.Tensor;

public abstract class AbstractHoverDemo extends LogWeightingDemo {
  final SpinnerLabel<Integer> spinnerCount;
  private final JButton jButtonShuffle = new JButton("shuffle");

  public AbstractHoverDemo() {
    super(false, ManifoldDisplays.MANIFOLDS, LogWeightings.list());
    setPositioningEnabled(false);
    {
      spinnerCount = SpinnerLabel.of(5, 10, 15, 20, 25, 30, 40);
      spinnerCount.setValue(25);
      addSpinnerListener(v -> shuffle(spinnerCount.getValue()));
      spinnerCount.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "magnify");
      spinnerCount.addSpinnerListener(this::shuffle);
    }
    {
      jButtonShuffle.addActionListener(e -> shuffle(spinnerCount.getValue()));
      timerFrame.jToolBar.add(jButtonShuffle);
    }
    shuffle(spinnerCount.getValue());
    setManifoldDisplay(Se2Display.INSTANCE);
    timerFrame.jToolBar.addSeparator();
  }

  protected void shuffle(int n) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::unproject));
    setControlPointsSe2(tensor);
  }

  @Override // from RenderInterface
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    Tensor origin = manifoldDisplay.project(mouse);
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
