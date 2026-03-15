// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascona.crv.CurvatureParam;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.red.Nest;

/** split interface and biinvariant mean based curve subdivision */
class CurveSubdivisionDemo extends PointSequenceDemo {
  @ReflectionMarker
  static class Param {
    public final CurvatureParam cp = new CurvatureParam();
    public CurveSubdivisionSchemes scheme = CurveSubdivisionSchemes.BSPLINE1;
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public Integer refine = 5;
    public Boolean line = false;
    public Boolean cyclic = false;
    public Boolean symi = true;
    public Boolean comb = true;
    public final CurveSubdivisionParam csp = CurveSubdivisionParam.GLOBAL;
  }

  private final Param param;

  public CurveSubdivisionDemo() {
    super(param = new Param());
  }

  @Override
  protected int initialCount() {
    return 2;
  }

  // this runnable causes to center (0,0) in the component center
  void center() {
    Dimension dimension = getSize();
    // IO.println("CALLED " + geometricComponent().getSize());
    Tensor pvm = PvmBuilder.rhs().setOffset(dimension.width / 2, dimension.height / 2).setPerPixel(100).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final CurveSubdivisionSchemes scheme = param.scheme;
    //
    if (scheme.equals(CurveSubdivisionSchemes.DODGSON_SABIN))
      setManifoldDisplay(ManifoldDisplays.R2);
    // ---
    if (param.symi) {
      Optional<SymMaskImages> optional = SymMaskImages.get(scheme.name());
      if (optional.isPresent()) {
        BufferedImage image0 = optional.get().image0();
        graphics.drawImage(image0, 0, 0, null);
        BufferedImage image1 = optional.get().image1();
        graphics.drawImage(image1, image0.getWidth() + 1, 0, null);
      }
    }
    // ---
    final boolean cyclic = param.cyclic || !scheme.isStringSupported();
    Tensor control = getGeodesicControlPoints();
    int levels = param.refine;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    try {
      Tensor refined = param.scheme.refine(manifoldDisplay, control, levels, cyclic);
      Tensor euclidXY = manifoldDisplay.point2xy().slash(refined);
      new PathRender(ColorStroke.CURVE, euclidXY, cyclic).render(geometricLayer, graphics);
      if (param.line) {
        GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
        Tensor refined2 = Nest.of(new BSpline1CurveSubdivision(geodesicSpace).auto(cyclic), control, 8);
        new PathRender(ColorStroke.SECONDARY_CURVE, refined2, cyclic) //
            .render(geometricLayer, graphics);
      }
      if (manifoldDisplay.isXYeuclid()) {
        Curvature2DRender.of(euclidXY, cyclic, param.comb).render(geometricLayer, graphics);
      }
      if (levels < 5)
        manifoldDisplay.showPoints(ColorPair.INTERMEDIATE, RealScalar.ONE, refined).render(geometricLayer, graphics);
      param.cp.spawnXY(manifoldDisplay, euclidXY, new Rectangle(0, 0, 400, 300)) //
          .render(geometricLayer, graphics);
    } catch (Exception exception) {
      // ---
      graphics.setColor(Color.RED);
      graphics.drawString("ERROR: " + exception.getMessage(), 0, 100);
      exception.printStackTrace();
    }
  }

  static void main() {
    new CurveSubdivisionDemo().runStandalone();
  }
}
