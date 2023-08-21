// code by jph
package ch.alpine.ascona.hull;

import java.awt.Graphics2D;
import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R3Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.SurfaceMeshRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.r3.ConvexHull3D;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.srf.SurfaceMesh;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

// TODO ASCONA generalize NdCenters
public class R3HullDemo extends AbstractDemo {
  private final ManifoldDisplay manifoldDisplay = R3Display.INSTANCE;
  private Tensor tensor;
  private final HullParam hullParam;
  private List<int[]> faces;

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
        RandomGenerator random = new SecureRandom();
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
    SurfaceMesh surfaceMesh = new SurfaceMesh(rotate, faces);
    new SurfaceMeshRender(surfaceMesh).render(geometricLayer, graphics);
  }

  public static void main(String[] args) {
    launch();
  }
}
