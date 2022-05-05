// code by jph
package ch.alpine.ascona.lev;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JToggleButton;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.ascona.util.dis.Spd2Display;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.bridge.win.AxesRender;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Biinvariants;
import ch.alpine.sophus.hs.VectorLogManifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.ArgMin;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

public class WeightsDemo extends LogWeightingDemo implements SpinnerListener<ManifoldDisplay> {
  private final JToggleButton jToggleAxes = new JToggleButton("axes");

  public WeightsDemo() {
    super(true, ManifoldDisplays.MANIFOLDS, LogWeightings.list());
    {
      timerFrame.jToolBar.add(jToggleAxes);
    }
    setControlPointsSe2(Tensors.fromString("{{-1, -2, 0}, {3, -2, -1}, {4, 2, 1}, {-1, 3, 2}, {-2, -3, -2}}"));
    ManifoldDisplay manifoldDisplay = Se2Display.INSTANCE;
    setGeodesicDisplay(manifoldDisplay);
    setLogWeighting(LogWeightings.DISTANCES);
    actionPerformed(manifoldDisplay);
    addSpinnerListener(this);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (jToggleAxes.isSelected())
      AxesRender.INSTANCE.render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    if (optional.isPresent()) {
      Tensor sequence = getSequence();
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      // ---
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderLevers();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
      // ---
      if (manifoldDisplay.dimensions() < sequence.length()) {
        Biinvariant metric = manifoldDisplay.metricBiinvariant();
        Biinvariant[] biinvariants = Objects.isNull(metric) //
            ? new Biinvariant[] { Biinvariants.LEVERAGES, Biinvariants.HARBOR, Biinvariants.GARDEN }
            : new Biinvariant[] { Biinvariants.LEVERAGES, Biinvariants.HARBOR, Biinvariants.GARDEN, metric };
        Tensor matrix = Tensors.empty();
        int[] minIndex = new int[biinvariants.length];
        VectorLogManifold vectorLogManifold = manifoldDisplay.hsManifold();
        for (int index = 0; index < biinvariants.length; ++index) {
          TensorUnaryOperator tensorUnaryOperator = //
              logWeighting().operator( //
                  biinvariants[index], //
                  vectorLogManifold, //
                  variogram(), //
                  sequence);
          Tensor weights = tensorUnaryOperator.apply(origin);
          minIndex[index] = ArgMin.of(weights);
          matrix.append(weights);
        }
        // System.out.println(Tensors.vectorInt(minIndex));
        // System.out.println("---");
        ColorDataIndexed colorDataIndexed = ColorDataLists._097.strict();
        for (int index = 0; index < sequence.length(); ++index) {
          Tensor map = matrix.get(Tensor.ALL, index).map(Tensors::of);
          leversRender.renderMatrix(sequence.get(index), map, colorDataIndexed);
        }
        int index = 0;
        graphics.setFont(LeversRender.FONT_MATRIX);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int fheight = fontMetrics.getAscent();
        for (Biinvariant biinvariant : biinvariants) {
          graphics.setColor(colorDataIndexed.getColor(index));
          graphics.drawString(biinvariant.toString(), 2, (index + 1) * fheight);
          ++index;
        }
      }
    }
  }

  @Override
  public void actionPerformed(ManifoldDisplay manifoldDisplay) {
    if (manifoldDisplay instanceof S2Display) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.346, -0.096, 0.262}, {-0.113, 0.858, 0.000}, {0.721, 0.288, -0.262}, {0.171, -0.038, 0.262}, {0.429, -0.646, -0.262}, {-0.804, -0.446, 0.524}, {-0.829, 0.513, -0.262}}"));
    }
    if (manifoldDisplay instanceof Spd2Display) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.325, -0.125, 1.309}, {-0.708, 1.475, -3.927}, {1.942, 1.075, -1.309}, {-0.308, -0.825, 4.974}, {-2.292, -0.608, 0.524}, {2.042, -0.625, -4.189}, {-4.108, 0.325, 1.309}}"));
    }
    System.out.println(manifoldDisplay.toString());
    if (manifoldDisplay.toString().startsWith("SE2")) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.563, -0.150, 6.545}, {4.783, 1.017, -0.785}, {-4.696, -0.650, 5.760}, {2.138, 0.600, 0.785}, {4.021, -0.550, 7.592}, {1.113, -1.208, 4.451}, {-0.154, -1.283, -1.309}, {-2.596, 0.933, 8.639}, {-2.429, -1.283, 7.854}, {-3.729, 0.483, 4.451}}"));
    }
  }

  public static void main(String[] args) {
    new WeightsDemo().setVisible(1200, 600);
  }
}
