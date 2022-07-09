// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.Optional;

import javax.swing.JCheckBox;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public class CoordinatesPlaceDemo extends LogWeightingDemo {
  private final JCheckBox jCheckBoxL = new JCheckBox("levers");
  private final JCheckBox jCheckBoxW = new JCheckBox("weights");

  public CoordinatesPlaceDemo() {
    super(true, ManifoldDisplays.ALL, LogWeightings.coordinates());
    timerFrame.jToolBar.add(jCheckBoxL);
    timerFrame.jToolBar.add(jCheckBoxW);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    if (optional.isPresent()) {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, getSequence(), optional.orElseThrow(), geometricLayer, graphics);
      if (jCheckBoxL.isSelected())
        leversRender.renderLevers();
      leversRender.renderSequence();
      leversRender.renderOrigin();
      // ---
      if (jCheckBoxW.isSelected())
        try {
          Tensor weights = operator(leversRender.getSequence()).sunder(leversRender.getOrigin());
          leversRender.renderWeights(weights);
        } catch (Exception exception) {
          System.err.println("no can do");
          // ---
        }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
