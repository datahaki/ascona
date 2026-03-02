// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascony.api.PolygonCoordinates;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.ascony.win.PlaceWrap;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

// TODO does not really work
class ThreePointBarycenterDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    public PolygonCoordinates polygonCoordinates = PolygonCoordinates.MEAN_VALUE;
  }

  private final Param param;

  public ThreePointBarycenterDemo() {
    super(param = new Param());
    spun();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = manifoldDisplay.manifold();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      leversRender.renderSequence();
      leversRender.renderTangentsXtoP(false);
      leversRender.renderPolygonXtoP();
      leversRender.renderLevers();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
      try {
        Sedarim sedarim = param.polygonCoordinates.sedarim(Biinvariants.USANCE.ofSafe(manifold), null, sequence);
        Tensor weights = sedarim.sunder(origin);
        leversRender.renderWeights(weights);
        HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
        // BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
        Tensor mean = homogeneousSpace.biinvariantMean().mean(sequence, weights);
        LeversRender.ORIGIN_RENDER_0 //
            .show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), Tensors.of(mean)) //
            .render(geometricLayer, graphics);
      } catch (Exception e) {
        System.err.println(e);
      }
    } else {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, placeWrap.getSequence(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  @SuppressWarnings("incomplete-switch")
  public void spun() {
    switch (getSelectedMD()) {
    case R2:
    case Td1: {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.175, 0.358, 0.000}, {-0.991, 0.113, 0.000}, {-0.644, 0.967, 0.000}, {0.509, 0.840, 0.000}, {0.689, 0.513, 0.000}, {0.956, -0.627, 0.000}}"));
      break;
    }
    case H2: {
      setControlPointsSe2(Tensors.fromString( //
          "{{0.200, 0.233, 0.000}, {-0.867, 2.450, 0.000}, {2.300, 2.117, 0.000}, {2.567, 0.150, 0.000}, {1.600, -2.583, 0.000}, {-2.550, -1.817, 0.000}}"));
      break;
    }
    case S2:
    case Rp2: {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.363, 0.388, 0.000}, {-0.825, -0.271, 0.000}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {-0.075, -0.733, 0.000}}"));
      break;
    }
    }
  }

  static void main() {
    new ThreePointBarycenterDemo().runStandalone();
  }
}
