// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Graphics2D;
import java.util.stream.IntStream;

import javax.swing.JToggleButton;

import ch.alpine.ascona.gbc.AbstractExportWeightingDemo;
import ch.alpine.ascona.util.api.ImageTiling;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

// TODO ASCONA enhance demo by showing the coordinate box from which functions are sampled
public class Se2ScatteredSetCoordinateDemo extends AbstractExportWeightingDemo {
  private static final Clip RANGE_X = Clips.absolute(3);
  private static final Clip RANGE_A = Clips.absolute(Pi.VALUE);
  // ---
  private final JToggleButton jToggleAxes = new JToggleButton("axes");

  public Se2ScatteredSetCoordinateDemo() {
    super(true, ManifoldDisplays.SE2C_SE2, LogWeightings.list());
    spinnerRefine.setValue(15);
    {
      jToggleAxes.setSelected(true);
      timerFrame.jToolBar.add(jToggleAxes);
    }
    Tensor se2 = Tensors.fromString("{{-1.5, 1.3, -2.3}, {+1.5, +1.3, 2.3}, {0.3, 1.5, 1.2}, {0.0, 0.5, -0.5}, {-1.4, -1.3, 0.1}, {1.2, -1.3, -1.2}}");
    // Tensor del = RandomVariate.of(UniformDistribution.of(0.00, 0.1),Dimensions.of(se2));
    setControlPointsSe2(se2);
    // Tensor model2pixel = timerFrame.geometricComponent.getModel2Pixel();
    // timerFrame.geometricComponent.setModel2Pixel(Tensors.vector(5, 5, 1).pmul(model2pixel));
    timerFrame.geometricComponent.setOffset(500, 500);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    // if (jToggleAxes.isSelected())
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    ColorDataGradient colorDataGradient = colorDataGradient();
    Tensor controlPoints = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, controlPoints, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    if (manifoldDisplay.dimensions() < controlPoints.length()) { // render basis functions
      Tensor origin = getGeodesicControlPoints();
      Tensor wgs = compute(operator(origin), refinement());
      RenderQuality.setQuality(graphics);
      Rescale rescale = new Rescale(ImageTiling.of(wgs));
      ArrayPlotImage.of(rescale.result(), rescale.scalarSummaryStatistics().getClip(), colorDataGradient).draw(graphics);
    }
  }

  private Tensor compute(Sedarim tensorUnaryOperator, int refinement) {
    Tensor sX = Subdivide.increasing(RANGE_X, refinement);
    Tensor sY = Subdivide.decreasing(RANGE_X, refinement);
    Tensor sA = Drop.tail(Subdivide.increasing(RANGE_A, 6), 1);
    int n = sX.length();
    Tensor origin = getGeodesicControlPoints(); // TODO ASCONA ALG
    Tensor wgs = Array.of(l -> DoubleScalar.INDETERMINATE, n * sA.length(), n, origin.length());
    IntStream.range(0, n).parallel().forEach(c0 -> {
      Scalar x = sX.Get(c0);
      int ofs = 0;
      for (Tensor a : sA) {
        int c1 = 0;
        for (Tensor y : sY) {
          Tensor point = Tensors.of(x, y, a);
          wgs.set(tensorUnaryOperator.sunder(point), ofs + c1, c0);
          ++c1;
        }
        ofs += n;
      }
    });
    return wgs;
  }

  public static void main(String[] args) {
    launch();
  }
}
