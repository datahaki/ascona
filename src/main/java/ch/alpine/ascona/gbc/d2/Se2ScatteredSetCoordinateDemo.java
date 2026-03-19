// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.stream.IntStream;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.ImageTiling;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ArrayPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.d2.ex.Box2D;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

final class Se2ScatteredSetCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  private static final Clip RANGE_X = Clips.absolute(3);
  private static final Clip RANGE_A = Clips.absolute(Pi.VALUE);

  public Se2ScatteredSetCoordinateDemo() {
    super(List.of(LogWeightings.values()));
    scatteredSetParam.refine = 15;
    Tensor se2 = Tensors.fromString( //
        "{{-1.5, 1.3, -2.3}, {+1.5, +1.3, 2.3}, {0.3, 1.5, 1.2}, {0.0, 0.5, -0.5}, {-1.4, -1.3, 0.1}, {1.2, -1.3, -1.2}}");
    setControlPointsSe2(se2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_SE2;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    ColorDataGradient colorDataGradient = scatteredSetParam.cdg;
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
      Tensor wgs = compute(weightingsParam.operator(manifoldDisplay.manifold(), origin), scatteredSetParam.refine);
      Tensor weights = ImageTiling.of(wgs);
      Show show = new Show();
      show.add(ArrayPlot.of(weights, colorDataGradient));
      show.render(graphics, new Rectangle(100, 10, 100 + Unprotect.dimension1Hint(weights) * 2, 400));
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
    new Se2ScatteredSetCoordinateDemo().runStandalone();
  }
}
