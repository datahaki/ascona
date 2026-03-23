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
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;

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
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
    setControlPointsSe2(INITIAL);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.ADDREMOVE;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    {
      Tensor points = getControlPointsSe2();
      if (0 < points.length()) {
        points.set(Array.zeros(3), 0);
        manifoldDisplay().showPoints(ColorPair.IMMOVABLE, RealScalar.ONE, Tensors.of(points.get(0))) //
            .render(geometricLayer, graphics);
      }
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor geodesicControlPoints = getGeodesicControlPoints();
    PlaceWrap placeWrap = new PlaceWrap(geodesicControlPoints);
    Optional<Tensor> optional = placeWrap.getOrigin();
    Tensor sequence = placeWrap.getSequence();
    if (optional.isPresent() && 2 < sequence.length()) {
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      LeversHud.render(Biinvariants.USANCE, leversRender, null);
      Manifold manifold = manifoldDisplay.manifold();
      try {
        Tensor matrix = new IterativeCoordinateMatrix(param.total).origin( //
            manifold.tangentSpace(origin).log().slash(sequence));
        Tensor circum = matrix.dot(sequence);
        leversRender.renderMatrix2(origin, matrix);
        LeversRender lr2 = LeversRender.of(manifoldDisplay, circum, origin, geometricLayer, graphics);
        lr2.renderSequence(ColorPair.SPLIT_PROCESS);
        lr2.renderIndexP("c");
      } catch (Exception exception) {
        System.err.println(exception.getMessage());
      }
    } else {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, geodesicControlPoints, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new IterativeCoordinateDemo().runStandalone();
  }
}
