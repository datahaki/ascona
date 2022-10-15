// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascona.util.api.CurveVisualSet;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2CoveringClothoidDisplay;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.JFreeChart;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidTransition;
import ch.alpine.sophus.crv.clt.ClothoidTransitionSpace;
import ch.alpine.sophus.crv.clt.LagrangeQuadraticD;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

/** demo compares conventional clothoid approximation with extended winding
 * number clothoid approximation to generate figures in report:
 * 
 * https://github.com/idsc-frazzoli/retina/files/3568308/20190903_appox_clothoids_with_ext_windings.pdf */
public class ClothoidComparisonDemo extends ControlPointsDemo {
  private static final int WIDTH = 480;
  private static final int HEIGHT = 360;
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(192);

  public ClothoidComparisonDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2C_ONLY));
    setControlPointsSe2(Tensors.fromString("{{0,0,0}, {3,0,0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    Tensor start = control.get(0);
    Tensor mouse = control.get(1);
    // ---
    ManifoldDisplay manifoldDisplay = Se2CoveringClothoidDisplay.INSTANCE;
    VisualSet visualSet = new VisualSet(ColorDataLists._097.cyclic().deriveWithAlpha(192));
    for (ClothoidTransitionSpace clothoidTransitionSpace : ClothoidTransitionSpace.values()) {
      int ordinal = clothoidTransitionSpace.ordinal();
      Color color = COLOR_DATA_INDEXED.getColor(ordinal);
      {
        graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        graphics.setColor(color);
        graphics.drawString(clothoidTransitionSpace.name(), 0, 24 + ordinal * 14);
      }
      ClothoidTransition clothoidTransition = clothoidTransitionSpace.connect(start, mouse);
      Clothoid clothoid = clothoidTransition.clothoid();
      Tensor points = clothoidTransition.linearized(RealScalar.of(geometricLayer.pixel2modelWidth(5)));
      new PathRender(color, 1.5f).setCurve(points, false).render(geometricLayer, graphics);
      // ---
      Tensor tensor = Tensor.of(points.stream().map(manifoldDisplay::point2xy));
      CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
      curveVisualSet.addCurvature(visualSet);
      {
        LagrangeQuadraticD curvature = clothoid.curvature();
        Tensor domain = curveVisualSet.getArcLength1();
        visualSet.add(domain, ConstantArray.of(curvature.head(), domain.length()));
        visualSet.add(domain, ConstantArray.of(curvature.tail(), domain.length()));
        visualSet.add(domain, Subdivide.of(0.0, 1.0, domain.length() - 1).map(curvature));
        visualSet.add(domain, Subdivide.of(0.0, 1.0, domain.length() - 1).map(clothoid::addAngle));
      }
    }
    JFreeChart jFreeChart = ListPlot.of(visualSet.setJoined(true));
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    jFreeChart.draw(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
  }

  public static void main(String[] args) {
    launch();
  }
}
