// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascony.ren.ColorStrokeIndexed;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.sophus.clt.Clothoid;
import ch.alpine.sophus.clt.ClothoidBuilder;
import ch.alpine.sophus.clt.ClothoidContext;
import ch.alpine.sophus.clt.ClothoidEmit;
import ch.alpine.sophus.clt.ClothoidSolutions;
import ch.alpine.sophus.clt.ClothoidTangentDefect;
import ch.alpine.sophus.clt.mid.MidpointTangentApproximation;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

/** shows several solutions to the clothoid fit problem
 * including the complex function over the real line */
class CustomClothoidDemo extends ClothoidBaseDemo {
  private static final Scalar MIN_RESOLUTION = RealScalar.of(0.05);
  private static final Scalar SCALE = RealScalar.of(0.1);

  public CustomClothoidDemo() {
    setControlPointsSe2(Tensors.fromString("{{0,0,0},{3,0,0}}"));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final Clip clip = Clips.absolute(30.0);
    final Tensor p = getGeodesicControlPoints().get(0);
    final Tensor q = getGeodesicControlPoints().get(1);
    final ClothoidContext clothoidContext = new ClothoidContext(p, q);
    final ClothoidTangentDefect clothoidTangentDefect = ClothoidTangentDefect.of(clothoidContext);
    final ClothoidSolutions clothoidSolutions = new ClothoidSolutions(clothoidTangentDefect, clip);
    // ---
    String params = "s1=" + clothoidContext.s1().maps(Round._4) + " s2=" + clothoidContext.s2().maps(Round._4);
    // ---
    {
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.of(clothoidContext.s1(), clothoidContext.s2())));
      graphics.setColor(Color.RED);
      graphics.fill(geometricLayer.toPath2D(CirclePoints.of(8).multiply(RealScalar.of(0.1))));
      geometricLayer.popMatrix();
    }
    {
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.cyclic().deriveWithAlpha(192);
      int index = 0;
      ColorStrokeIndexed colorStrokeIndexed = new ColorStrokeIndexed(colorDataIndexed, new BasicStroke(1.25f));
      for (Tensor _lambda : clothoidSolutions.lambdas()) {
        ClothoidBuilder clothoidBuilder = CustomClothoidBuilder.of((Scalar) _lambda);
        ClothoidTransition clothoidTransition = ClothoidTransition.of(clothoidBuilder, clothoidContext.p(), clothoidContext.q());
        Tensor points = clothoidTransition.linearized(MIN_RESOLUTION);
        new PathRender(colorStrokeIndexed.getColorStroke(index), points, false) //
            .render(geometricLayer, graphics);
        ++index;
      }
    }
    // ---
    Show show = ClothoidTangentDefectShow.of(clothoidContext, clip).getShow();
    show.setShowLabel(params);
    Tensor lambdas = clothoidSolutions.lambdas();
    List<Clothoid> clothoids = ClothoidEmit.stream(clothoidContext, lambdas).toList();
    {
      Tensor points = Tensors.empty();
      for (int index = 0; index < lambdas.length(); ++index) {
        Scalar lambda = lambdas.Get(index);
        Clothoid clothoid = clothoids.get(index);
        Scalar length = clothoid.length();
        points.append(Tensors.of(lambda, length.multiply(SCALE)));
      }
      show.add(ListPlot.of(points));
    }
    {
      Scalar s1 = clothoidContext.b0().add(clothoidContext.b1()).multiply(Rational.HALF);
      Scalar reifs = MidpointTangentApproximation.ORDER2.apply(clothoidContext.b0(), clothoidContext.b1()).subtract(s1);
      Tensor points = Tensors.of(Tensors.of(reifs, RealScalar.ONE));
      show.add(ListPlot.of(points));
    }
    Dimension dimension = geometricComponent().getSize();
    show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width, dimension.height / 2));
  }

  static void main() {
    new CustomClothoidDemo().runStandalone();
  }
}
