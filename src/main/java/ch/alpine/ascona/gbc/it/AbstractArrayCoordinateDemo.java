// code by jph
package ch.alpine.ascona.gbc.it;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.gbc.d2.AbstractExportWeightingDemo;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

/* package */ abstract class AbstractArrayCoordinateDemo extends AbstractExportWeightingDemo {
  private ArrayPlotImage arrayPlotImage;

  protected AbstractArrayCoordinateDemo(List<LogWeighting> array) {
    super(true, ManifoldDisplays.d2Rasters(), array);
    // ---
    addMouseRecomputation();
  }

  @Override
  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    arrayPlotImage = manifoldDisplay.dimensions() < sequence.length() //
        ? arrayPlotImage(sequence, refinement(), operator(sequence)::sunder)
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
    if (Objects.isNull(arrayPlotImage))
      recompute();
    if (Objects.nonNull(arrayPlotImage))
      arrayPlotImage.draw(graphics);
  }
}
