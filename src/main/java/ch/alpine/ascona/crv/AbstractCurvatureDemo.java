// code by jph
package ch.alpine.ascona.crv;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.api.CurveVisualSet;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

/** class is used in other projects outside of owl */
public abstract class AbstractCurvatureDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  @ReflectionMarker
  public static class AbstractCurvatureParam extends AsconaParam {
    public AbstractCurvatureParam(List<ManifoldDisplays> list) {
      super(true, list);
    }

    public Boolean graph = this instanceof BufferedImageSupplier;
    public Boolean curvt = true;
  }

  private final AbstractCurvatureParam abstractCurvatureParam;

  protected AbstractCurvatureDemo(AbstractCurvatureParam abstractCurvatureParam) {
    super(abstractCurvatureParam);
    this.abstractCurvatureParam = abstractCurvatureParam;
  }

  @Override
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor refined = protected_render(geometricLayer, graphics);
    if (abstractCurvatureParam.graph && this instanceof BufferedImageSupplier bufferedImageSupplier)
      graphics.drawImage(bufferedImageSupplier.bufferedImage(), 0, 0, null);
    if (abstractCurvatureParam.curvt && 1 < refined.length()) {
      Tensor tensor = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
      Show show = new Show(COLOR_DATA_INDEXED);
      CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
      curveVisualSet.addCurvature(show);
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      show.render(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
    }
  }

  protected abstract Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics);
}
