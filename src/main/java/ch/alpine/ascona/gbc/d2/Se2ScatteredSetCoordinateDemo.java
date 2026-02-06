// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.stream.IntStream;

import javax.swing.JToggleButton;

import ch.alpine.ascony.api.Box2D;
import ch.alpine.ascony.api.ImageTiling;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.arp.ArrayPlotImage;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.dv.Sedarim;
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

public class Se2ScatteredSetCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  private static final Clip RANGE_X = Clips.absolute(3);
  private static final Clip RANGE_A = Clips.absolute(Pi.VALUE);
  // ---
  private final JToggleButton jToggleAxes = new JToggleButton("axes");

  public Se2ScatteredSetCoordinateDemo() {
    super(ManifoldDisplays.SE2C_SE2, LogWeightings.list());
    scatteredSetParam.refine = 15;
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
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    ColorDataGradient colorDataGradient = scatteredSetParam.spinnerColorData;
    Tensor controlPoints = getGeodesicControlPoints();
    {
      Tensor box = Box2D.polygon(Box2D.xy(RANGE_X));
      Path2D path2d = geometricLayer.toPath2D(box, true);
      graphics.setColor(Color.LIGHT_GRAY);
      graphics.draw(path2d);
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, controlPoints, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    if (manifoldDisplay.dimensions() < controlPoints.length()) { // render basis functions
      Tensor origin = getGeodesicControlPoints();
      // TODO ASCONA use cache
      Tensor wgs = compute(operator(origin), scatteredSetParam.refine);
      RenderQuality.setQuality(graphics);
      Rescale rescale = new Rescale(ImageTiling.of(wgs));
      ArrayPlotImage.of(rescale.result(), rescale.clip(), colorDataGradient).draw(graphics);
    }
  }

  private Tensor compute(Sedarim tensorUnaryOperator, int refinement) {
    Tensor sX = Subdivide.increasing(RANGE_X, refinement);
    Tensor sY = Subdivide.decreasing(RANGE_X, refinement);
    Tensor sA = Drop.tail(Subdivide.increasing(RANGE_A, 6), 1);
    int n = sX.length();
    Tensor origin = getGeodesicControlPoints(); // TODO ASCONA ALG
    Tensor wgs = Array.of(_ -> DoubleScalar.INDETERMINATE, n * sA.length(), n, origin.length());
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

  static void main() {
    launch();
  }
}
