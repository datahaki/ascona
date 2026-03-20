// code by jph
package ch.alpine.ascona.hull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.SurfaceMeshRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.qhull3.PlatonicSolid;
import ch.alpine.sophis.ref.d2.SurfaceMeshRefinement;
import ch.alpine.sophis.ref.d2.SurfaceMeshRefinements;
import ch.alpine.sophis.srf.SurfaceMesh;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;

class SurfaceMeshDemo extends ControlPointsDemo {
  public static SurfaceMesh surfaceMesh(PlatonicSolid platonicSolid) {
    return new SurfaceMesh(platonicSolid.vertices(), platonicSolid.faces());
  }

  @ReflectionMarker
  public static class Param {
    public Boolean ctrl = true;
    public SurfaceMeshRefinements ref = SurfaceMeshRefinements.CATMULL_CLARK;
    @FieldSlider
    @FieldPreferredWidth(100)
    @FieldClip(min = "0", max = "4")
    public Integer refine = 2;
    public ColorDataGradients cdg = ColorDataGradients.CLASSIC;
  }

  private final Param param;
  private final SurfaceMesh surfaceMesh;

  public SurfaceMeshDemo() {
    super(param = new Param());
    // ---
    surfaceMesh = surfaceMesh(PlatonicSolid.ICOSAHEDRON);
    setControlPointsSe2(surfaceMesh.vrt);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_R2_H2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    surfaceMesh.vrt = getGeodesicControlPoints();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    SurfaceMeshRefinement surfaceMeshRefinement = param.ref.operator(homogeneousSpace.biinvariantMean());
    SurfaceMesh refine = surfaceMesh;
    for (int count = 0; count < param.refine; ++count)
      refine = surfaceMeshRefinement.refine(refine);
    new SurfaceMeshRender(refine, param.cdg).render(geometricLayer, graphics);
    {
      // TODO ASCONA levers render
      graphics.setColor(new Color(192, 192, 192, 192));
      Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.5));
      for (Tensor mean : refine.vrt) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(mean));
        graphics.fill(geometricLayer.toPath2D(shape));
        geometricLayer.popMatrix();
      }
    }
    if (param.ctrl) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      Tensor domain = Subdivide.of(0.0, 1.0, 10);
      Set<Tensor> set = new HashSet<>();
      for (int[] array : surfaceMesh.faces()) {
        for (int index = 0; index < array.length; ++index) {
          int beg = array[index];
          int end = array[(index + 1) % array.length];
          if (set.add(Sort.of(Tensors.vector(beg, end)))) {
            ScalarTensorFunction scalarTensorFunction = //
                geodesicSpace.curve(surfaceMesh.vrt.get(beg), surfaceMesh.vrt.get(end));
            Tensor points = domain.maps(scalarTensorFunction);
            new PathRender(ColorStroke.CURVE, points, false).render(geometricLayer, graphics);
          }
        }
      }
    }
  }

  static void main() {
    new SurfaceMeshDemo().runStandalone();
  }
}
