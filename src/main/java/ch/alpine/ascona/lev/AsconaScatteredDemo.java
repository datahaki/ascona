// code by jph
package ch.alpine.ascona.lev;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.dis.Se2CoveringDisplay;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;

public class AsconaScatteredDemo extends LogWeightingDemo implements SpinnerListener<ManifoldDisplay> {
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  private final JToggleButton jToggleNeutral = new JToggleButton("neutral");

  public AsconaScatteredDemo() {
    super(true, ManifoldDisplays.MANIFOLDS, LogWeightings.list());
    // ---
    spinnerColorData.setValue(ColorDataGradients.TEMPERATURE);
    spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(200, 28), "color scheme");
    // ---
    timerFrame.jToolBar.add(jToggleNeutral);
    // ---
    ManifoldDisplay manifoldDisplay = Se2CoveringDisplay.INSTANCE;
    manifoldDisplay = R2Display.INSTANCE;
    setManifoldDisplay(manifoldDisplay);
    setBitype(Biinvariants.METRIC);
    actionPerformed(manifoldDisplay);
    addManifoldListener(this);
    jToggleNeutral.setSelected(true);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    Tensor sequence = getSequence();
    if (optional.isPresent()) {
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      ColorDataGradient colorDataGradient = spinnerColorData.getValue().deriveWithOpacity(RealScalar.of(0.5));
      leversRender.renderOrigin();
      leversRender.renderSequence();
      leversRender.renderIndexP();
      leversRender.renderIndexX();
      Sedarim sedarim = Biinvariants.METRIC.of(RnGroup.INSTANCE).coordinate(InversePowerVariogram.of(2), sequence);
      try {
        Tensor weights = sedarim.sunder(origin);
        leversRender.renderWeights(weights);
      } catch (Exception e) {
        // TODO ASCONA does not work for Spd2 SO3 He1
        System.err.println("no can do: " + manifoldDisplay);
      }
      // LeversHud.render(bitype(), leversRender, colorDataGradient);
    }
  }

  public static final Tensor SOME = Tensors.matrix(new Number[][] { //
      { 0, 0, 0 }, //
      { -0.583, -2.317, 0.000 }, //
      { -2.133, -0.933, 0.000 }, //
      { -1.317, 1.567, 0.000 }, //
      { 1.800, 1.033, 0.000 }, //
      { 3.267, -0.550, 0.000 }, //
      { 2.583, -2.133, 0.000 } //
  }).unmodifiable();

  @Override
  public void actionPerformed(ManifoldDisplay manifoldDisplay) {
    setControlPointsSe2(SOME);
  }

  public static void main(String[] args) {
    new AsconaScatteredDemo().setVisible(1200, 900);
  }
}
