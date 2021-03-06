// code by jph
package ch.alpine.ascona.crv.d2;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.d2.PolygonCentroid;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.sophus.hs.r3.MinTriangleAreaSquared;
import ch.alpine.sophus.math.AppendOne;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.red.Times;

/** Reference:
 * "Polygon Laplacian Made Simple"
 * by Astrid Bunge, Philipp Herholz, Misha Kazhdan, Mario Botsch, 2020 */
public class MinTriangleAreaSquaredDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();
  // ---
  private final PathRender pathRender = new PathRender(COLOR_DATA_INDEXED.getColor(1), 1.5f);

  public MinTriangleAreaSquaredDemo() {
    super(new AsconaParam(true, ManifoldDisplays.R2_ONLY));
    // ---
    timerFrame.geometricComponent.addRenderInterface(pathRender);
    // ---
    Tensor blub = Tensors.fromString("{{1, 0, 0}, {0, 1, 0}, {2, 0, 2.5708}, {1, 0, 2.1}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 0), //
        Tensor.of(blub.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    pathRender.setCurve(sequence, true);
    if (0 < sequence.length()) {
      Tensor polygon = Tensor.of(sequence.stream().map(AppendOne.FUNCTION));
      Tensor weights = MinTriangleAreaSquared.INSTANCE.origin(polygon);
      {
        Tensor origin = weights.dot(polygon).extract(0, 2);
        LeversRender leversRender = //
            LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
        leversRender.renderSequence();
        leversRender.renderIndexP();
        leversRender.renderWeights(weights);
        leversRender.renderOrigin();
        leversRender.renderLevers(weights);
      }
      {
        Tensor origin = PolygonCentroid.of(sequence);
        LeversRender leversRender = //
            LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
        leversRender.renderOrigin();
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
