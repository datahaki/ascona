// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.api.PolygonCoordinates;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.sca.Chop;

// TODO ASCONA demo becomes unstable when control points are removed
public class ThreePointBarycenterDemo extends LogWeightingDemo implements SpinnerListener<ManifoldDisplays> {
  private final JToggleButton jToggleNeutral = new JToggleButton("neutral");

  public ThreePointBarycenterDemo() {
    super(true, ManifoldDisplays.d2Rasters(), List.of(PolygonCoordinates.values()));
    // ---
    timerFrame.jToolBar.add(jToggleNeutral);
    // ---
    ManifoldDisplays manifoldDisplays = ManifoldDisplays.S2;
    setManifoldDisplay(manifoldDisplays);
    spun(manifoldDisplays);
    addManifoldListener(this);
    jToggleNeutral.setSelected(true);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = getSequence();
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
        Sedarim sedarim = operator(sequence);
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
          LeversRender.of(manifoldDisplay, getSequence(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  @Override
  public void spun(ManifoldDisplays manifoldDisplays) {
    if (manifoldDisplays.equals(ManifoldDisplays.R2)) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.175, 0.358, 0.000}, {-0.991, 0.113, 0.000}, {-0.644, 0.967, 0.000}, {0.509, 0.840, 0.000}, {0.689, 0.513, 0.000}, {0.956, -0.627, 0.000}}"));
    } else
      if (manifoldDisplays.equals(ManifoldDisplays.H2)) {
        setControlPointsSe2(Tensors.fromString( //
            "{{0.200, 0.233, 0.000}, {-0.867, 2.450, 0.000}, {2.300, 2.117, 0.000}, {2.567, 0.150, 0.000}, {1.600, -2.583, 0.000}, {-2.550, -1.817, 0.000}}"));
      } else //
        if (manifoldDisplays.equals(ManifoldDisplays.S2)) {
          setControlPointsSe2(Tensors.fromString( //
              "{{-0.363, 0.388, 0.000}, {-0.825, -0.271, 0.000}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {-0.075, -0.733, 0.000}}"));
        }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new ThreePointBarycenterDemo().setVisible(1200, 900);
  }
}
