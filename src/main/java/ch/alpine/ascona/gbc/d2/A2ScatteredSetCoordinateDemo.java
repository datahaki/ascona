// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import javax.swing.JToggleButton;

import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.win.AxesRender;
import ch.alpine.bridge.win.RenderInterface;
import ch.alpine.tensor.Tensor;

/* package */ abstract class A2ScatteredSetCoordinateDemo extends AbstractExportWeightingDemo {
  private final JToggleButton jToggleAxes = new JToggleButton("axes");
  // ---
  private RenderInterface renderInterface;

  public A2ScatteredSetCoordinateDemo(List<LogWeighting> array) {
    super(true, ManifoldDisplays.R2_H2_S2, array);
    // ---
    timerFrame.jToolBar.add(jToggleAxes);
    jToggleHeatmap.setVisible(false);
    jToggleArrows.setVisible(false);
    addMouseRecomputation();
  }

  @Override
  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    renderInterface = manifoldDisplay.dimensions() < sequence.length() //
        ? arrayPlotRender(sequence, refinement(), operator(sequence), magnification())
        : null;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (jToggleAxes.isSelected())
      AxesRender.INSTANCE.render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    renderControlPoints(geometricLayer, graphics);
    {
      final Tensor sequence = getGeodesicControlPoints();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay(), sequence, null, geometricLayer, graphics);
      leversRender.renderIndexX();
      leversRender.renderIndexP();
    }
    // ---
    if (Objects.isNull(renderInterface))
      recompute();
    if (Objects.nonNull(renderInterface))
      renderInterface.render(geometricLayer, graphics);
  }
}