// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JToggleButton;

import ch.alpine.ascona.lev.LogWeightingDemo;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;

public abstract class AbstractScatteredSetWeightingDemo extends LogWeightingDemo {
  protected final SpinnerLabel<Integer> spinnerRefine;
  private final SpinnerLabel<Integer> spinnerMagnif;
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  protected final JToggleButton jToggleHeatmap = new JToggleButton("heatmap");
  protected final JToggleButton jToggleArrows = new JToggleButton("arrows");

  public AbstractScatteredSetWeightingDemo( //
      boolean addRemoveControlPoints, //
      List<ManifoldDisplays> list, //
      List<LogWeighting> array) {
    super(addRemoveControlPoints, list, array);
    setMidpointIndicated(false);
    spinnerLogWeighting.addSpinnerListener(v -> recompute());
    {
      spinnerRefine = SpinnerLabel.of(3, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 120, 160);
      spinnerRefine.setValue(20);
      spinnerRefine.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "refinement");
      spinnerRefine.addSpinnerListener(v -> recompute());
    }
    {
      spinnerMagnif = SpinnerLabel.of(1, 2, 3, 4);
      spinnerMagnif.setValue(2);
      spinnerMagnif.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "magnify");
      spinnerMagnif.addSpinnerListener(v -> recompute());
    }
    spinnerColorData.setValue(ColorDataGradients.CLASSIC);
    spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(120, 28), "color scheme");
    spinnerColorData.addSpinnerListener(v -> recompute());
    {
      jToggleHeatmap.setSelected(true);
      timerFrame.jToolBar.add(jToggleHeatmap);
      jToggleArrows.setSelected(false);
      timerFrame.jToolBar.add(jToggleArrows);
    }
    // ---
    timerFrame.jToolBar.addSeparator();
  }

  protected final int refinement() {
    return spinnerRefine.getValue();
  }

  protected final int magnification() {
    return spinnerMagnif.getValue();
  }

  protected final ColorDataGradient colorDataGradient() {
    return spinnerColorData.getValue();
  }
}
