// code by jph
package ch.alpine.ascona.crv;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.CurveVisualSet;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

/** class is used in other projects outside of owl */
@ReflectionMarker
public abstract class AbstractCurvatureDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;
  // ---
  public Boolean graph = this instanceof BufferedImageSupplier;
  public Boolean curvt = true;

  public AbstractCurvatureDemo() {
    this(ManifoldDisplays.ALL);
  }

  public AbstractCurvatureDemo(List<ManifoldDisplays> list) {
    super(true, list);
  }

  @Override
  public synchronized final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor refined = protected_render(geometricLayer, graphics);
    if (graph && this instanceof BufferedImageSupplier bufferedImageSupplier)
      graphics.drawImage(bufferedImageSupplier.bufferedImage(), 0, 0, null);
    if (curvt && 1 < refined.length()) {
      Tensor tensor = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
      VisualSet visualSet = new VisualSet(COLOR_DATA_INDEXED);
      CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
      curveVisualSet.addCurvature(visualSet);
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      ListPlot.of(visualSet, true).draw(graphics, new Rectangle2D.Double(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
    }
  }

  protected abstract Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics);
}
