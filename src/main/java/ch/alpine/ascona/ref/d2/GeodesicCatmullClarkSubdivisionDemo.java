// code by jph
package ch.alpine.ascona.ref.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
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
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.red.Nest;

public class GeodesicCatmullClarkSubdivisionDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "0", max = "4")
    public Integer refine = 2;
  }

  private final Param param;

  public GeodesicCatmullClarkSubdivisionDemo() {
    this(new Param());
  }

  public GeodesicCatmullClarkSubdivisionDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 0, 0}, {2, 0, 0}, {0, 1, 0}, {1, 1, 0}, {2, 1, 0}}").multiply(RealScalar.of(2)));
  }

  @Override
  public List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected boolean addRemoveControlPoints() {
    return false;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    int dim = control.get(0).length();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    GeodesicCatmullClarkSubdivision catmullClarkSubdivision = //
        new GeodesicCatmullClarkSubdivision(geodesicSpace);
    Tensor refined = Nest.of( //
        catmullClarkSubdivision::refine, //
        ArrayReshape.of(control, 2, 3, dim), //
        param.refine);
    // TODO ASCONA LR
    Tensor shape = manifoldDisplay.shape();
    for (Tensor point : Flatten.of(refined, 1)) {
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
      Path2D path2d = geometricLayer.toPath2D(shape);
      geometricLayer.popMatrix();
      int rgb = 128 + 32;
      path2d.closePath();
      graphics.setColor(new Color(rgb, rgb, rgb, 128 + 64));
      graphics.fill(path2d);
      graphics.setColor(Color.BLACK);
      graphics.draw(path2d);
    }
  }

  static void main() {
    new GeodesicCatmullClarkSubdivisionDemo().runStandalone();
  }
}
