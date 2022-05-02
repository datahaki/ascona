// code by jph
package ch.alpine.ascona.lev;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascona.api.PolygonCoordinates;
import ch.alpine.ascona.dis.H2Display;
import ch.alpine.ascona.dis.ManifoldDisplay;
import ch.alpine.ascona.dis.ManifoldDisplays;
import ch.alpine.ascona.dis.R2Display;
import ch.alpine.ascona.dis.S2Display;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;

/* package */ class ThreePointBarycenterDemo extends LogWeightingDemo implements SpinnerListener<ManifoldDisplay> {
  private final JToggleButton jToggleNeutral = new JToggleButton("neutral");

  public ThreePointBarycenterDemo() {
    super(true, ManifoldDisplays.R2_H2_S2, Arrays.asList(PolygonCoordinates.values()));
    // ---
    timerFrame.jToolBar.add(jToggleNeutral);
    // ---
    ManifoldDisplay manifoldDisplay = S2Display.INSTANCE;
    setGeodesicDisplay(manifoldDisplay);
    actionPerformed(manifoldDisplay);
    addSpinnerListener(this);
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
        TensorUnaryOperator tensorUnaryOperator = operator(sequence);
        Tensor weights = tensorUnaryOperator.apply(origin);
        leversRender.renderWeights(weights);
        BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
        Tensor mean = biinvariantMean.mean(sequence, weights);
        LeversRender.ORIGIN_RENDER_0 //
            .show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), Tensors.of(mean)) //
            .render(geometricLayer, graphics);
      } catch (Exception e) {
        System.err.println(e);
      }
    } else {
      renderControlPoints(geometricLayer, graphics);
    }
  }

  @Override
  public void actionPerformed(ManifoldDisplay manifoldDisplay) {
    if (manifoldDisplay instanceof R2Display) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.175, 0.358, 0.000}, {-0.991, 0.113, 0.000}, {-0.644, 0.967, 0.000}, {0.509, 0.840, 0.000}, {0.689, 0.513, 0.000}, {0.956, -0.627, 0.000}}"));
    } else
      if (manifoldDisplay instanceof H2Display) {
        setControlPointsSe2(Tensors.fromString( //
            "{{0.200, 0.233, 0.000}, {-0.867, 2.450, 0.000}, {2.300, 2.117, 0.000}, {2.567, 0.150, 0.000}, {1.600, -2.583, 0.000}, {-2.550, -1.817, 0.000}}"));
      } else //
        if (manifoldDisplay instanceof S2Display) {
          setControlPointsSe2(Tensors.fromString( //
              "{{-0.363, 0.388, 0.000}, {-0.825, -0.271, 0.000}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {-0.075, -0.733, 0.000}}"));
        }
  }

  public static void main(String[] args) {
    new ThreePointBarycenterDemo().setVisible(1200, 900);
  }
}
