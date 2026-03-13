// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Graphics2D;
import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversHud;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.ascony.win.PlaceWrap;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.gbc.d2.IterativeCoordinateMatrix;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

class IterativeCoordinateDemo extends EuclideanPlaneDemo {
  public static final Tensor INITIAL = Tensors.matrix(new Number[][] { //
      { 0, 0, 0 }, //
      { -0.583, -2.317, 0.000 }, //
      { -2.133, -0.933, 0.000 }, //
      { -1.317, 1.567, 0.000 }, //
      { 1.800, 1.033, 0.000 }, //
      { 3.267, -0.550, 0.000 }, //
      { 2.583, -2.133, 0.000 } //
  }).unmodifiable();

  @ReflectionMarker
  static class Param {
    @FieldClip(min = "0", max = "20")
    public Integer total = 2;
  }

  private final Param param;

  public IterativeCoordinateDemo() {
    super(param = new Param());
    // ---
    setControlPointsSe2(INITIAL);
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    Tensor sequence = placeWrap.getSequence();
    if (optional.isPresent() && 2 < sequence.length()) {
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      LeversHud.render(Biinvariants.USANCE, leversRender, null);
      HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
      Manifold manifold = homogeneousSpace;
      try {
        Tensor matrix = new IterativeCoordinateMatrix(param.total).origin( //
            manifold.tangentSpace(origin).log().slash(sequence));
        Tensor circum = matrix.dot(sequence);
        // new PointsRender(color_fill, color_draw).show(matrixLift, shape, points);
        // new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 255)) //
        // .show(geodesicDisplay::matrixLift, geodesicDisplay.shape(), circum) //
        // .render(geometricLayer, graphics);
        leversRender.renderMatrix2(origin, matrix);
        LeversRender lr2 = LeversRender.of(manifoldDisplay, circum, origin, geometricLayer, graphics);
        lr2.renderSequence(ColorPair.SPLIT_PROCESS);
        lr2.renderIndexP("c");
      } catch (Exception exception) {
        System.err.println(exception.getMessage());
      }
    } else {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new IterativeCoordinateDemo().runStandalone();
  }
}
