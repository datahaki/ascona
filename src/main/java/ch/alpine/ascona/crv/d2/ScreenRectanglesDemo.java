// code by jph
package ch.alpine.ascona.crv.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.awt.ScreenRectangles;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;

public class ScreenRectanglesDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }
  }

  public ScreenRectanglesDemo() {
    this(new Param());
  }

  public ScreenRectanglesDemo(Param param) {
    super(param);
  }

  private static Rectangle here(Point2D p, Point2D q) {
    int x = (int) p.getX();
    int y = (int) p.getY();
    int width = Math.max(0, (int) (q.getX() - p.getX()));
    int height = Math.max(0, (int) (q.getY() - p.getY()));
    return new Rectangle(x, y, width, height);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    {
      int length = control.length() / 2;
      if (0 < length) {
        Iterator<Tensor> iterator = control.iterator();
        Rectangle window = here(geometricLayer.toPoint2D(iterator.next()), geometricLayer.toPoint2D(iterator.next()));
        graphics.setColor(new Color(0, 255, 0, 128));
        graphics.fill(window);
        List<Rectangle> list = new LinkedList<>();
        for (int len = 1; len < length; ++len) {
          Rectangle rectangle = here(geometricLayer.toPoint2D(iterator.next()), geometricLayer.toPoint2D(iterator.next()));
          graphics.setColor(new Color(255, 0, 0, 128));
          graphics.fill(rectangle);
          list.add(rectangle);
        }
        ScreenRectangles screenRectangles = new ScreenRectangles(list);
        Rectangle allVisible = screenRectangles.allVisible(window);
        graphics.setColor(new Color(0, 0, 255, 128));
        graphics.fill(allVisible);
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new ScreenRectanglesDemo().runStandalone();
  }
}
