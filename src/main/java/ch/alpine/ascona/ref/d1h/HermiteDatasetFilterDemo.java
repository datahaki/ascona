// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.math.Do;
import ch.alpine.sophis.ref.d1h.Hermite3Filter;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.lie.se2.Se2BiinvariantMeans;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;

class HermiteDatasetFilterDemo extends AbstractHermiteDatasetDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);

  @ReflectionMarker
  static class Paran {
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6" })
    public Integer level = 5;
    public Boolean adjoint = false;
    public Boolean derivat = true;
  }

  private final Paran paran;

  public HermiteDatasetFilterDemo() {
    super(paran = new Paran());
  }

  @SuppressWarnings("unused")
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      final Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.3));
      new PathRender(ColorStroke.CURVE, _control.get(Tensor.ALL, 0), false).render(geometricLayer, graphics);
      if (_control.length() <= 1000)
        for (Tensor point : _control.get(Tensor.ALL, 0)) {
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
          Path2D path2d = geometricLayer.toPath2D(shape);
          path2d.closePath();
          graphics.setColor(new Color(255, 128, 128, 64));
          graphics.fill(path2d);
          graphics.setColor(COLOR_CURVE);
          graphics.draw(path2d);
          geometricLayer.popMatrix();
        }
    }
    graphics.setColor(Color.DARK_GRAY);
    Scalar delta = getDelta();
    TensorIteration tensorIteration = //
        // new Hermite1Filter(Se2Group.INSTANCE, Se2CoveringExponential.INSTANCE).string(delta, _control);
        new Hermite3Filter(Se2Group.INSTANCE, Se2BiinvariantMeans.FILTER) //
            .string(delta, _control);
    int levels = 2 * paran.level;
    Tensor refined = Do.of(_control, tensorIteration::iterate, levels);
    manifoldDisplay.showPoints(ColorPair.APPROXIMATION, RealScalar.of(0.3), refined.get(Tensor.ALL, 0));
    new PathRender(ColorStroke.SECONDARY_CURVE, refined.get(Tensor.ALL, 0), false) //
        .render(geometricLayer, graphics);
    if (paran.derivat) {
      Tensor deltas = refined.get(Tensor.ALL, 1);
      int dims = deltas.get(0).length();
      if (0 < deltas.length()) {
        Show show = StaticHelper.listPlot(deltas, delta, levels);
        Dimension dimension = getSize();
        show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
      }
    }
  }

  static void main() {
    new HermiteDatasetFilterDemo().runStandalone();
  }
}
