// code by jph
package ch.alpine.ascona.lev;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradients;

public class AffineDemo extends AbstractPlaceDemo implements SpinnerListener<ManifoldDisplays> {
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  private final JToggleButton jToggleNeutral = new JToggleButton("neutral");

  public AffineDemo() {
    super(true, ManifoldDisplays.R2_ONLY);
    // ---
    spinnerColorData.setValue(ColorDataGradients.TEMPERATURE);
    spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(200, 28), "color scheme");
    // ---
    timerFrame.jToolBar.add(jToggleNeutral);
    // ---
    ManifoldDisplays manifoldDisplays = ManifoldDisplays.R2;
    setManifoldDisplay(manifoldDisplays);
    spun(manifoldDisplays);
    addManifoldListener(this);
    jToggleNeutral.setSelected(true);
    setMidpointIndicated(true);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = getSequence();
      Tensor origin = optional.orElseThrow();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
      leversRender.renderOrigin();
      leversRender.renderIndexX();
      // Tensor weights = AffineCoordinate.of(vectorLogManifold).weights(sequence, origin);
      // leversRender.renderWeights(weights);
      Tensor doma = Subdivide.of(0.0, 1.0, 11);
      for (int index = 0; index < sequence.length(); ++index) {
        Tensor prev = sequence.get(Math.floorMod(index - 1, sequence.length()));
        Tensor next = sequence.get(index);
        Tensor curve = doma.map(manifoldDisplay.geodesicSpace().curve(prev, next));
        Tensor polygon = Tensor.of(curve.stream().map(manifoldDisplay::point2xy));
        Path2D path2d = geometricLayer.toPath2D(polygon);
        graphics.draw(path2d);
      }
    }
  }

  @Override
  public void spun(ManifoldDisplays manifoldDisplays) {
    if (manifoldDisplays.equals(ManifoldDisplays.R2)) {
      setControlPointsSe2(Tensors.fromString("{{0.3, 1, 0}, {0, 0, 0}, {1, 0, 0}}"));
    } else
      if (manifoldDisplays.equals(ManifoldDisplays.S2)) {
        setControlPointsSe2(Tensors.fromString( //
            "{{0.300, 0.092, 0.000}, {-0.563, -0.658, 0.262}, {-0.854, -0.200, 0.000}, {-0.746, 0.663, -0.262}, {0.467, 0.758, 0.262}, {0.446, -0.554, 0.262}}"));
        setControlPointsSe2(Tensors.fromString( //
            "{{-0.521, 0.621, 0.262}, {-0.863, 0.258, 0.000}, {-0.725, 0.588, -0.785}, {0.392, 0.646, 0.000}, {-0.375, 0.021, 0.000}, {-0.525, -0.392, 0.000}}"));
        setControlPointsSe2(Tensors.fromString( //
            "{{-0.583, 0.338, 0.000}, {-0.904, -0.258, 0.262}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {0.396, -0.688, 0.000}}"));
        setControlPointsSe2(Tensors.fromString( //
            "{{-0.363, 0.388, 0.000}, {-0.825, -0.271, 0.000}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {-0.075, -0.733, 0.000}}"));
      } else //
        if (manifoldDisplays.equals(ManifoldDisplays.Se2)) {
          setControlPointsSe2(Tensors.fromString(
              "{{3.150, -2.700, -0.524}, {-1.950, -3.683, 0.000}, {-1.500, -1.167, 2.094}, {4.533, -0.733, -1.047}, {8.567, -3.300, -1.309}, {2.917, -5.050, -1.047}}"));
        }
  }

  public static void main(String[] args) {
    new AffineDemo().setVisible(1200, 900);
  }
}
