// code by jph
package ch.alpine.ascona.hull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R3Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.r2.SignedCurvature2D;
import ch.alpine.sophus.hs.r3.qh3.ConvexHull3D;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Sign;

public class R3HullDemo extends AbstractDemo {
  private final ManifoldDisplay manifoldDisplay = R3Display.INSTANCE;
  private Tensor tensor;
  private final HullParam hullParam;
  private int[][] faces;

  public R3HullDemo() {
    this(new HullParam());
  }

  public R3HullDemo(HullParam hullParam) {
    super(hullParam);
    this.hullParam = hullParam;
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    if (hullParam.shuffle) {
      hullParam.shuffle = false;
      int n = hullParam.count.number().intValue();
      if (hullParam.cuboid) {
        Clip[] clips = { Clips.absoluteOne(), Clips.absoluteOne(), Clips.absoluteOne() };
        CoordinateBoundingBox ccb = CoordinateBoundingBox.of(clips);
        Random random = new Random();
        tensor = RandomSample.of(BoxRandomSample.of(ccb), random, n);
        for (int index = 0; index < tensor.length(); ++index) {
          int i = random.nextInt(3);
          if (random.nextBoolean())
            tensor.set(clips[i].min(), index, i);
          else
            tensor.set(clips[i].max(), index, i);
        }
      } else {
        tensor = RandomSample.of(manifoldDisplay.randomSampleInterface(), n);
      }
    }
    faces = ConvexHull3D.of(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (hullParam.quality)
      RenderQuality.setQuality(graphics);
    Tensor rotate = this.tensor.dot(hullParam.rotation());
    LeversRender leversRender = LeversRender.of(manifoldDisplay, rotate, null, geometricLayer, graphics);
    leversRender.renderSequence();
    for (int[] face : faces) {
      Tensor polygon = Tensor.of(IntStream.of(face).mapToObj(rotate::get));
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
