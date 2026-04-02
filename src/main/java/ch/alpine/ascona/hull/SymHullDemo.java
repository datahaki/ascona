// code by jph
package ch.alpine.ascona.hull;

import java.awt.Container;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.ren.SurfaceMeshRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.hull.d3.ConvexHull3D;
import ch.alpine.sophis.srf.SurfaceMesh;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.col.ColorDataGradients;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.pow.Sqrt;

@ReflectionMarker
class SymHullDemo implements ManipulateProvider, RenderInterface {
  public final SymParam symParam = new SymParam();
  // ---
  private Tensor tensor;
  private List<int[]> faces;
  private final GeometricComponent geometricComponent = new GeometricComponent();

  public SymHullDemo() {
    geometricComponent.addRenderInterface(this);
    Tensor pvm = PvmBuilder.rhs().setPerPixel(RealScalar.of(200)).setOffset(300, 300).digest();
    geometricComponent.setModel2Pixel(pvm);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    int layers = symParam.layers;
    int n = symParam.n;
    tensor = Tensors.empty();
    for (Tensor _z : Subdivide.of(-0.9, 0.9, layers)) {
      Scalar z = (Scalar) _z;
      Scalar r = Sqrt.FUNCTION.apply(RealScalar.ONE.subtract(z.multiply(z)));
      CirclePoints.of(n).stream().map(xy -> xy.multiply(r).append(z)).forEach(tensor::append);
    }
    faces = ConvexHull3D.of(tensor);
    Tensor rotate = this.tensor.dot(symParam.rotParam.rotation());
    SurfaceMesh surfaceMesh = new SurfaceMesh(rotate, faces);
    new SurfaceMeshRender(surfaceMesh, ColorDataGradients.AURORA).render(geometricLayer, graphics);
  }

  @Override
  public Container getContainer() {
    return geometricComponent;
  }

  static void main() {
    new SymHullDemo().runStandalone();
  }
}
