// code by jph
package ch.alpine.ascona.hull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.SignedCurvature2D;
import ch.alpine.tensor.opt.qh3.ConvexHull3D;
import ch.alpine.tensor.sca.Sign;

public class S2HullDemo extends AbstractDemo {
  private final ManifoldDisplay manifoldDisplay = S2Display.INSTANCE;
  private Tensor tensor;
  // private ConvexHull3D hull;
  private final HullParam hullParam;
  private int[][] faces;

  public S2HullDemo() {
    this(new HullParam());
  }

  public S2HullDemo(HullParam hullParam) {
    super(hullParam);
    this.hullParam = hullParam;
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    if (hullParam.shuffle) {
      hullParam.shuffle = false;
      int n = hullParam.count.number().intValue();
      tensor = RandomSample.of(manifoldDisplay.randomSampleInterface(), n);
      // RandomPermutation.of(123);
      faces = ConvexHull3D.of(tensor);
      // hull = new ConvexHull3D();
      // hull.build(tensor);
    }
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (hullParam.quality)
      RenderQuality.setQuality(graphics);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, tensor, null, geometricLayer, graphics);
    leversRender.renderSequence();
    // Tensor pnts =
    // hull.getVertices2();
    for (int[] face : faces) {
      Tensor polygon = Tensor.of(IntStream.of(face).mapToObj(tensor::get));
      Optional<Scalar> optional = SignedCurvature2D.of( //
          polygon.get(0).extract(0, 2), //
          polygon.get(1).extract(0, 2), //
          polygon.get(2).extract(0, 2));
      boolean ccw = optional.isPresent() && Sign.isPositive(optional.orElseThrow());
      Path2D path2d = geometricLayer.toPath2D(polygon);
      path2d.closePath();
      if (!ccw == hullParam.ccw) {
        graphics.setColor(ccw ? new Color(0, 255, 0, 64) : new Color(0, 0, 255, 64));
        graphics.fill(path2d);
        graphics.setColor(Color.BLACK);
        graphics.draw(path2d);
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
