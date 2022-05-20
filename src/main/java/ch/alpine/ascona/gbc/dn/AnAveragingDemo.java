// code by jph
package ch.alpine.ascona.gbc.dn;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JToggleButton;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.lev.LogWeightingDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.chq.FiniteScalarQ;

public abstract class AnAveragingDemo extends LogWeightingDemo {
  private static final int WIDTH = 480;
  private static final int HEIGHT = 360;
  // ---
  private final JToggleButton jToggleButton = new JToggleButton("varplot");

  public AnAveragingDemo(List<ManifoldDisplay> geodesicDisplays) {
    super(true, geodesicDisplays, LogWeightings.averagings());
    setMidpointIndicated(false);
    spinnerLogWeighting.addSpinnerListener(v -> recompute());
    // ---
    jToggleButton.setSelected(false);
    timerFrame.jToolBar.add(jToggleButton);
  }

  protected final boolean isDeterminate() {
    return FiniteScalarQ.of(variogram().apply(RealScalar.ZERO));
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    protected_render(geometricLayer, graphics);
    if (jToggleButton.isSelected()) {
      ScalarUnaryOperator variogram = variogram();
      VisualSet visualSet = new VisualSet();
      Tensor domain = Subdivide.of(isDeterminate() ? 0.0 : 0.1, 3.0, 100);
      visualSet.add(domain, domain.map(variogram));
      JFreeChart jFreeChart = ListPlot.of(visualSet);
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      jFreeChart.draw(graphics, new Rectangle2D.Double(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  public abstract void protected_render(GeometricLayer geometricLayer, Graphics2D graphics);
}
