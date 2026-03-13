// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.EmptyRender;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;

/** class is used in other projects outside of owl */
@ReflectionMarker
public final class CurvatureParam {
  public Boolean curvt = true;

  public final RenderInterface spawn(ManifoldDisplay manifoldDisplay, Tensor refined, Rectangle rectangle) {
    return curvt && 1 < refined.length() //
        ? new RenderInterface() {
          @Override
          public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
            Show show = new Show();
            Tensor tensor = manifoldDisplay.point2xy().slash(refined);
            CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
            curveVisualSet.addCurvature(show);
            show.render_autoIndent(graphics, rectangle);
          }
        }
        : EmptyRender.INSTANCE;
  }
}
