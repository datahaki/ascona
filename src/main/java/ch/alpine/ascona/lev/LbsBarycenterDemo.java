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
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

/** Visualization of
 * "Spherical Barycentric Coordinates"
 * by Torsten Langer, Alexander Belyaev, Hans-Peter Seidel, 2005 */
public class LbsBarycenterDemo extends LogWeightingDemo implements SpinnerListener<ManifoldDisplays> {
  private final JToggleButton jToggleNeutral = new JToggleButton("neutral");

  public LbsBarycenterDemo() {
    super(true, ManifoldDisplays.S2_ONLY, List.of(PolygonCoordinates.values()));
    // ---
    timerFrame.jToolBar.add(jToggleNeutral);
    // ---
    ManifoldDisplays manifoldDisplays = ManifoldDisplays.S2;
    setManifoldDisplay(manifoldDisplays);
    actionPerformed(manifoldDisplays);
    addManifoldListener(this);
    jToggleNeutral.setSelected(true);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    Tensor sequence = getSequence();
    if (optional.isPresent() && 0 < sequence.length()) {
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      // ---
      leversRender.renderSurfaceP();
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderLbsS2();
      leversRender.renderLevers();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
      // ---
      geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.vector(3, 0)));
      manifoldDisplay().background().render(geometricLayer, graphics);
      // ---
      leversRender.renderSurfaceP();
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderTangentsXtoP(false);
      leversRender.renderPolygonXtoP();
      leversRender.renderLevers();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
      // ---
      geometricLayer.popMatrix();
    } else {
      {
        LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
        leversRender.renderSequence();
      }
    }
  }

  @Override
  public void actionPerformed(ManifoldDisplays manifoldDisplays) {
    if (manifoldDisplays.equals(ManifoldDisplays.S2)) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.314, 0.662, 0.000}, {-0.809, 0.426, 0.000}, {-0.261, 0.927, 0.000}, {0.564, 0.685, 0.000}, {0.694, 0.220, 0.000}}"));
    }
  }

  public static void main(String[] args) {
    new LbsBarycenterDemo().setVisible(1200, 900);
  }
}
