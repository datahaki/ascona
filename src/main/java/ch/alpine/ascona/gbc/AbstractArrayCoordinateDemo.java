// code by jph
package ch.alpine.ascona.gbc;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.arp.ArrayPlotRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public abstract class AbstractArrayCoordinateDemo extends AbstractExportWeightingDemo {
  private ArrayPlotRender arrayPlotRender;

  public AbstractArrayCoordinateDemo(List<LogWeighting> array) {
    super(true, ManifoldDisplays.ARRAYS, array);
    // ---
    jToggleHeatmap.setVisible(false);
    jToggleArrows.setVisible(false);
    addMouseRecomputation();
  }

  @Override
  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    arrayPlotRender = manifoldDisplay.dimensions() < sequence.length() //
        ? arrayPlotRender(sequence, refinement(), operator(sequence), magnification())
        : null;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
    }
    // ---
    if (Objects.isNull(arrayPlotRender))
      recompute();
    if (Objects.nonNull(arrayPlotRender))
      arrayPlotRender.render(graphics);
  }
}
