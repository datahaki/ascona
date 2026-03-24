// code by jph
package ch.alpine.ascona.lev;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascony.api.PolygonCoordinates;
import ch.alpine.ascony.dat.PlaceWrap;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;

class ThreePointBarycenterDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    public PolygonCoordinates polygonCoordinates = PolygonCoordinates.MEAN_VALUE;
  }

  private final Param param;

  public ThreePointBarycenterDemo() {
    super(param = new Param());
    addChangeListener(this::spun);
    spun();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYHIDE;
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
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderSurfaceP();
      leversRender.renderTangentsXtoP(false);
      leversRender.renderPolygonXtoP();
      leversRender.renderLevers();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
      // check is necessary due to originEnclosure
      if (Unprotect.dimension1Hint(sequence) == 2) {
        Sedarim sedarim = param.polygonCoordinates.sedarim(Biinvariants.USANCE.ofSafe(manifold), null, sequence);
        Tensor weights = sedarim.sunder(origin);
        leversRender.renderWeights(weights);
        HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
        Optional<Tensor> optionalMean = homogeneousSpace.biinvariantMean().optional(sequence, weights);
        if (optionalMean.isPresent()) {
          Tensor mean = optionalMean.orElseThrow();
          manifoldDisplay.showPoints(ColorPair.MARKER, RealScalar.of(1.2), Tensors.of(mean)) //
              .render(geometricLayer, graphics);
        } else {
          graphics.setColor(Color.RED);
          graphics.drawString("mean does not exist", 100, 20);
        }
      }
    } else {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, placeWrap.getSequence(), null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

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
    default:
      throw new IllegalArgumentException();
    }
  }

  static void main() {
    new ThreePointBarycenterDemo().runStandalone();
  }
}
