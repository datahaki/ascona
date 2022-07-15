// code by jph
package ch.alpine.ascona.gbc;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public abstract class AbstractArrayCoordinateDemo extends AbstractExportWeightingDemo {
  private ArrayPlotImage arrayPlotRender;

  public AbstractArrayCoordinateDemo(List<LogWeighting> array) {
    super(true, ManifoldDisplays.d2Rasters(), array);
    // ---
    addMouseRecomputation();
  }

  @Override
  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    arrayPlotRender = manifoldDisplay.dimensions() < sequence.length() //
        ? arrayPlotRender(sequence, refinement(), operator(sequence)::sunder)
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
      arrayPlotRender.draw(graphics);
  }
}
