// code by jph
package ch.alpine.ascona.flt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;

import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.GeodesicFilters;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.TensorProduct;
import ch.alpine.tensor.sca.win.WindowFunctions;

// TODO ASCONA DEMO visualization can be improved much
public class GeodesicFiltersDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DRAW = ColorDataLists._001.strict();
  private static final ColorDataIndexed COLOR_FILL = COLOR_DRAW.deriveWithAlpha(64);
  // ---
  protected final SpinnerLabel<WindowFunctions> spinnerKernel = new SpinnerLabel<>();

  public GeodesicFiltersDemo() {
    super(true, ManifoldDisplays.SE2C_SE2_R2);
    // ---
    timerFrame.jToolBar.addSeparator();
    {
      spinnerKernel.setList(Arrays.asList(WindowFunctions.values()));
      spinnerKernel.setValue(WindowFunctions.GAUSSIAN);
      spinnerKernel.addToComponentReduced(timerFrame.jToolBar, new Dimension(180, 28), "smoothing kernel");
    }
    setControlPointsSe2(TensorProduct.of(Range.of(0, 5), UnitVector.of(3, 0)).multiply(RealScalar.of(2)));
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    if (!Integers.isEven(control.length())) {
      ScalarUnaryOperator smoothingKernel = spinnerKernel.getValue().get();
      for (GeodesicFilters geodesicFilters : GeodesicFilters.values()) {
        int ordinal = geodesicFilters.ordinal();
        Tensor mean = geodesicFilters.from(manifoldDisplay, smoothingKernel).apply(control);
        Color color = COLOR_DRAW.getColor(ordinal);
        PointsRender pointsRender = new PointsRender(COLOR_FILL.getColor(ordinal), color);
        pointsRender.show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), Tensors.of(mean)).render(geometricLayer, graphics);
        graphics.setColor(color);
        graphics.drawString("" + geodesicFilters, 0, 32 + ordinal * 16);
      }
    }
  }

  public static void main(String[] args) {
    new GeodesicFiltersDemo().setVisible(1000, 600);
  }
}
