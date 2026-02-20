// code by jph
package ch.alpine.ascona.ref.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.Arrowhead;
import ch.alpine.sophis.ref.d2.GeodesicCatmullClarkSubdivision;
import ch.alpine.sophus.math.api.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ArrayReshape;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.red.Nest;

public class GeodesicCatmullClarkSubdivisionDemo extends ControlPointsDemo {
  private static final Tensor ARROWHEAD_LO = Arrowhead.of(0.18);

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.SE2C_SE2);
    }

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

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    GeodesicCatmullClarkSubdivision catmullClarkSubdivision = //
        new GeodesicCatmullClarkSubdivision(geodesicSpace);
    Tensor refined = Nest.of( //
        catmullClarkSubdivision::refine, //
        ArrayReshape.of(control, 2, 3, 3), //
        param.refine);
    RenderQuality.setQuality(graphics);
    // TODO ASCONA LR
    for (Tensor point : Flatten.of(refined, 1)) {
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
      Path2D path2d = geometricLayer.toPath2D(ARROWHEAD_LO);
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
