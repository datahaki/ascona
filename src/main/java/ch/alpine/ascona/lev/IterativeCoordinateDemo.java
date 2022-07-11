// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.Optional;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.gbc.d2.IterativeCoordinateMatrix;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.HsDesign;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;

public class IterativeCoordinateDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    @FieldInteger
    @FieldClip(min = "2", max = "20")
    public Scalar total = RealScalar.of(2);
  }

  private final Param param;

  public IterativeCoordinateDemo() {
    this(new Param());
  }

  public IterativeCoordinateDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    ManifoldDisplays manifoldDisplays = ManifoldDisplays.R2;
    setManifoldDisplay(manifoldDisplays);
    setControlPointsSe2(R2PointCollection.SOME);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    Tensor sequence = placeWrap.getSequence();
    if (optional.isPresent() && 2 < sequence.length()) {
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      LeversHud.render(Biinvariants.LEVERAGES, leversRender, null);
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      Manifold manifold = homogeneousSpace;
      HsDesign hsDesign = new HsDesign(manifold);
      try {
        Tensor matrix = new IterativeCoordinateMatrix(param.total.number().intValue()).origin(hsDesign.matrix(sequence, origin));
        Tensor circum = matrix.dot(sequence);
        // new PointsRender(color_fill, color_draw).show(matrixLift, shape, points);
        // new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 255)) //
        // .show(geodesicDisplay::matrixLift, geodesicDisplay.shape(), circum) //
        // .render(geometricLayer, graphics);
        leversRender.renderMatrix2(origin, matrix);
        LeversRender lr2 = LeversRender.of(manifoldDisplay, circum, origin, geometricLayer, graphics);
        lr2.renderSequence();
        lr2.renderIndexP("c");
      } catch (Exception exception) {
        System.err.println(exception.getMessage());
      }
    } else {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
