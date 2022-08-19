// code by jph
package ch.alpine.ascona.hull;

import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R3Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.SurfaceMeshRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.r3.ConvexHull3D;
import ch.alpine.sophus.srf.SurfaceMesh;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.pow.Sqrt;

// TODO ASCONA generalize NdCenters
public class SymHullDemo extends AbstractDemo {
  private final ManifoldDisplay manifoldDisplay = R3Display.INSTANCE;
  private Tensor tensor;
  private final SymParam hullParam;
  private List<int[]> faces;

  public SymHullDemo() {
    this(new SymParam());
  }

  public SymHullDemo(SymParam hullParam) {
    super(hullParam);
    this.hullParam = hullParam;
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    int layers = hullParam.layers;
    int n = hullParam.n;
    tensor = Tensors.empty();
    for (Tensor _z : Subdivide.of(-0.9, 0.9, layers)) {
      Scalar z = (Scalar) _z;
      Scalar r = Sqrt.FUNCTION.apply(RealScalar.ONE.subtract(z.multiply(z)));
      CirclePoints.of(n).stream().map(xy -> xy.multiply(r).append(z)).forEach(tensor::append);
    }
    // tensor = empty;
    faces = ConvexHull3D.of(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
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
