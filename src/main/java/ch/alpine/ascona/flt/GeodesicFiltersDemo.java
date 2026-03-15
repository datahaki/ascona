// code by jph
package ch.alpine.ascona.flt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairIndexed;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.win.WindowFunctions;

// TODO ASCONA DEMO visualization can be improved much
class GeodesicFiltersDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DRAW = ColorDataLists._001.strict();

  @ReflectionMarker
  static class Param {
    public WindowFunctions windowFunctions = WindowFunctions.DIRICHLET;
  }

  private final Param param;

  public GeodesicFiltersDemo() {
    this(new Param());
  }

  public GeodesicFiltersDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    Tensor tensor = Tensors.fromString("{{0, 0, 0}, {2, 2, 0}, {4, 0, 0}, {6, 0, 0}, {8, 0, -1}}");
    setControlPointsSe2(tensor);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_SE2_R2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    if (Integers.isOdd(control.length())) {
      ScalarUnaryOperator smoothingKernel = param.windowFunctions.get();
      ColorPairIndexed colorPairIndexed = new ColorPairIndexed(COLOR_DRAW, 64, 255);
      for (GeodesicFilters geodesicFilters : GeodesicFilters.values()) {
        int ordinal = geodesicFilters.ordinal();
        try {
          Tensor mean = geodesicFilters.supply(manifoldDisplay.geodesicSpace(), smoothingKernel).apply(control);
          manifoldDisplay.showPoints(colorPairIndexed.getColorPair(ordinal), RealScalar.ONE, Tensors.of(mean)).render(geometricLayer, graphics);
          Color color = COLOR_DRAW.getColor(ordinal);
          graphics.setColor(color);
          graphics.drawString("" + geodesicFilters, 0, 32 + ordinal * 16);
        } catch (Exception e) {
          graphics.setColor(Color.RED);
          graphics.drawString("" + geodesicFilters + "  fail", 0, 32 + ordinal * 16);
        }
      }
    }
  }

  static void main() {
    new GeodesicFiltersDemo().runStandalone();
  }
}
