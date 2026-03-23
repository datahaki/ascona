// code by jph
package ch.alpine.ascona.hull;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
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
import ch.alpine.sophis.hull.d3.PlatonicSolid;
import ch.alpine.sophis.ref.d2.SurfaceMeshRefinement;
import ch.alpine.sophis.ref.d2.SurfaceMeshRefinements;
import ch.alpine.sophis.srf.SurfaceMesh;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.nrm.Vector2Norm;

class SurfaceMeshDemo extends ControlPointsDemo {
  public static SurfaceMesh surfaceMesh(PlatonicSolid platonicSolid) {
    return new SurfaceMesh(platonicSolid.vertices(), platonicSolid.faces());
  }

  @ReflectionMarker
  static class Param {
    public PlatonicSolid mesh = PlatonicSolid.ICOSAHEDRON;
  }

  @ReflectionMarker
  static class Paran {
    public Boolean ctrl = true;
    public Boolean inter = true;
    public SurfaceMeshRefinements ref = SurfaceMeshRefinements.DOO_SABIN;
    @FieldSlider
    @FieldPreferredWidth(100)
    @FieldClip(min = "0", max = "4")
    public transient Integer refine = 1;
    public transient ColorDataGradients cdg = ColorDataGradients.COPPER;
  }

  private final Param param;
  private final Paran paran;
  private SurfaceMesh surfaceMesh;

  public SurfaceMeshDemo() {
    super(param = new Param(), paran = new Paran());
    // ---
    fieldsEditor(param).addUniversalListener(this::compute);
    addChangeListener(this::compute);
    compute();
  }

  private void compute() {
    surfaceMesh = surfaceMesh(param.mesh);
    if (getSelectedMD().equals(ManifoldDisplays.S2))
      surfaceMesh.vrt = Vector2Norm.NORMALIZE.slash(surfaceMesh.vrt);
    setControlPointsSe2(surfaceMesh.vrt);
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_R3_S2;
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
    SurfaceMeshRefinement surfaceMeshRefinement = paran.ref.operator(homogeneousSpace.biinvariantMean());
    SurfaceMesh refine = surfaceMesh;
    for (int count = 0; count < paran.refine; ++count)
      refine = surfaceMeshRefinement.refine(refine);
    new SurfaceMeshRender(refine, paran.cdg).render(geometricLayer, graphics);
    if (paran.inter)
      manifoldDisplay.showPoints(ColorPair.APPROXIMATION, Rational.HALF, refine.vrt) //
          .render(geometricLayer, graphics);
    if (paran.ctrl) {
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
