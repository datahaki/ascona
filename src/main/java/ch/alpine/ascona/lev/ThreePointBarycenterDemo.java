// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.Optional;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.PolygonCoordinates;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.sca.Chop;

public class ThreePointBarycenterDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.d2Rasters());
    }

    public PolygonCoordinates polygonCoordinates = PolygonCoordinates.MEAN_VALUE;
  }

  private final Param param;

  public ThreePointBarycenterDemo() {
    this(new Param());
  }

  public ThreePointBarycenterDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    spun();
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
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
        Sedarim sedarim = param.polygonCoordinates.sedarim(Biinvariants.LEVERAGES.ofSafe(manifold), null, sequence);
        Tensor weights = sedarim.sunder(origin);
        leversRender.renderWeights(weights);
        HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
        BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
        Tensor mean = biinvariantMean.mean(sequence, weights);
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
    switch (asconaParam().manifoldDisplays) {
    case R2:
    case T1d: {
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

  public static void main(String[] args) {
    launch();
  }
}
