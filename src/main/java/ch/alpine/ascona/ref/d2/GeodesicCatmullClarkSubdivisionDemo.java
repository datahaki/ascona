// code by jph
package ch.alpine.ascona.ref.d2;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.ref.d2.GeodesicCatmullClarkSubdivision;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ArrayReshape;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.red.Nest;

class GeodesicCatmullClarkSubdivisionDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    @FieldSlider
    @FieldClip(min = "0", max = "4")
    public Integer refine = 2;
  }

  private final Param param;

  public GeodesicCatmullClarkSubdivisionDemo() {
    super(param = new Param());
    // ---
    Tensor vrt = Tensors.fromString("{{-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {-1, 1, 0}, {0, 1, 0}, {1, 1, 0}}");
    setControlPointsSe2(vrt.multiply(RealScalar.of(0.3)));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds2DimOrMore();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    GeodesicCatmullClarkSubdivision catmullClarkSubdivision = //
        new GeodesicCatmullClarkSubdivision(geodesicSpace);
    List<Integer> dims = new ArrayList<Integer>();
    dims.add(2);
    dims.add(3);
    dims.addAll(Dimensions.of(control.get(0)));
    Tensor refined = Nest.of( //
        catmullClarkSubdivision::refine, //
        ArrayReshape.of(control, dims), //
        param.refine);
    manifoldDisplay.showPoints(ColorPair.ABE, RealScalar.of(0.5), Flatten.of(refined, 1)) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    new GeodesicCatmullClarkSubdivisionDemo().runStandalone();
  }
}
