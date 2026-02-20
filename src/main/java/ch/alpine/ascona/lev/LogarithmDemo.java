// code by jph
package ch.alpine.ascona.lev;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.itp.ArcLengthParameterization;
import ch.alpine.sophis.ref.d1.CurveSubdivision;
import ch.alpine.sophis.ref.d1.FourPointCurveSubdivision;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.rn.RGroup;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.sophus.lie.so2.ArcTan2D;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;

public class LogarithmDemo extends ControlPointsDemo {
  private static final GridRender GRID_RENDER = new GridRender(Tensors.vector(-1, 0, 1), Color.LIGHT_GRAY);
  private static final Color DOMAIN_F = new Color(192, 192, 64, 64);
  private static final Color DOMAIN_D = new Color(192, 192, 64, 192);

  @ReflectionMarker
  public static class Param {
    @FieldClip(min = "5", max = "20")
    public Integer length = 11;
    public Boolean ctrl = false;
  }

  private final Param param;

  public LogarithmDemo() {
    this(new Param()); // for 2 dimensional
  }

  public LogarithmDemo(Param param) {
    super(new AsconaParam(true, ManifoldDisplays.d2Rasters()), param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    fieldsEditor(0).addUniversalListener(this::spun);
    spun();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      {
        LeversRender leversRender = //
            LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
        leversRender.renderOrigin();
        leversRender.renderIndexX();
        // ---
        if (param.ctrl) {
          leversRender.renderIndexP();
          leversRender.renderSequence(); // toggle show
        }
      }
      // ---
      CurveSubdivision curveSubdivision = //
          new FourPointCurveSubdivision(homogeneousSpace);
      if (2 < sequence.length()) {
        Tensor refined = sequence;
        while (refined.length() < 100)
          refined = curveSubdivision.cyclic(refined);
        // ---
        RenderQuality.setQuality(graphics);
        if (manifoldDisplay instanceof R2Display)
          GRID_RENDER.render(geometricLayer, graphics);
        {
          RenderQuality.setQuality(graphics);
          Path2D path2d = geometricLayer.toPath2D(Tensor.of(refined.stream().map(manifoldDisplay::point2xy)), true);
          graphics.setColor(DOMAIN_F);
          graphics.fill(path2d);
          graphics.setColor(DOMAIN_D);
          graphics.draw(path2d);
        }
        final Tensor domain = Drop.tail(Subdivide.of(0.0, 1.0, param.length), 1);
        geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(10, 0)));
        GRID_RENDER.render(geometricLayer, graphics);
        Tensor planar = homogeneousSpace.exponential(origin).log().slash(refined);
        {
          RenderQuality.setQuality(graphics);
          Path2D path2d = geometricLayer.toPath2D(planar, true);
          graphics.setColor(DOMAIN_F);
          graphics.fill(path2d);
          graphics.setColor(DOMAIN_D);
          graphics.draw(path2d);
        }
        Tensor angles_acc = Tensor.of(planar.stream().map(ArcTan2D::of));
        Tensor distances = Differences.of(angles_acc).maps(So2.MOD);
        try {
          ScalarTensorFunction scalarTensorFunction = ArcLengthParameterization.of(distances, RGroup.INSTANCE, planar);
          Tensor border = domain.maps(scalarTensorFunction);
          RenderQuality.setQuality(graphics);
          graphics.setColor(Color.BLUE);
          for (Tensor y : border) {
            Line2D line2d = geometricLayer.toLine2D(y);
            graphics.draw(line2d);
          }
        } catch (Exception exception) {
          exception.printStackTrace();
        }
        geometricLayer.popMatrix();
        try {
          ScalarTensorFunction scalarTensorFunction = ArcLengthParameterization.of(distances, homogeneousSpace, refined);
          Tensor border = domain.maps(scalarTensorFunction);
          LeversRender leversRender = LeversRender.of( //
              manifoldDisplay, //
              border, origin, //
              geometricLayer, graphics);
          leversRender.renderLevers();
        } catch (Exception exception) {
          exception.printStackTrace();
        }
      }
    }
  }

  public void spun() {
    switch (asconaParam().manifoldDisplays) {
    case R2:
    case T1d:
      setControlPointsSe2(Tensors.fromString( //
          "{{0.358, 0.508, 0.000}, {-0.375, -0.567, 0.000}, {0.442, -0.425, 0.000}, {1.142, 0.000, 0.000}, {1.158, 1.108, 0.000}, {0.192, 1.433, 0.000}, {-0.625, 0.342, 0.000}}"));
      break;
    case H2:
      // setControlPointsSe2(Tensors.fromString( //
      // "{{1.033, -1.267, 0.000}, {0.567, -2.433, 0.000}, {1.967, -1.967, 0.000}, {2.067, -0.583, 0.000}, {-0.017, -0.450, 0.000}, {-0.700, -1.017, 0.000}}"));
      setControlPointsSe2(Tensors.fromString( //
          "{{1.350, -0.558, 0.000}, {0.558, -1.175, 0.000}, {2.350, -1.233, 0.000}, {1.975, 0.208, 0.000}, {1.600, 1.175, 0.000}, {0.567, 0.467, 0.000}}"));
      break;
    case S2:
    case Rp2:
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.325, -0.500, 0.262}, {-0.225, -0.917, 0.262}, {0.556, -0.496, 0.262}, {0.708, 0.417, 0.262}, {-0.177, 0.088, 0.262}, {-0.792, 0.358, 0.262}, {-0.867, -0.258, 0.000}}"));
      break;
    default:
      System.err.println("should not happen");
    }
  }

  static void main() {
    new LogarithmDemo().runStandalone();
  }
}
