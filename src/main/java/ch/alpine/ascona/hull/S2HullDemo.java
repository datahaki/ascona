// code by jph
package ch.alpine.ascona.hull;

import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.SurfaceMeshRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.r3.ConvexHull3D;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.srf.SurfaceMesh;
import ch.alpine.tensor.Tensor;

public class S2HullDemo extends AbstractDemo {
  private final ManifoldDisplay manifoldDisplay = S2Display.INSTANCE;
  private Tensor tensor;
  private final HullParam hullParam;
  private List<int[]> faces;

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
      faces = ConvexHull3D.of(tensor);
    }
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
