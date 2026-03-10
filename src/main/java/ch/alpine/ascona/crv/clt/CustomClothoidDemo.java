// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JLabel;

import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidContext;
import ch.alpine.sophis.crv.clt.ClothoidEmit;
import ch.alpine.sophis.crv.clt.ClothoidSolutions;
import ch.alpine.sophis.crv.clt.ClothoidTangentDefect;
import ch.alpine.sophis.crv.clt.mid.MidpointTangentApproximation;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

class CustomClothoidDemo extends ClothoidBaseDemo {
  private static final Tensor INITIAL = Tensors.fromString("{{0,0,0},{3,0,0}}");
  private final JLabel jLabel = new JLabel();
  private static final Scalar MIN_RESOLUTION = RealScalar.of(0.05);
  private static final Scalar SCALE = RealScalar.of(0.1);
  // ---

  public CustomClothoidDemo() {
    setControlPointsSe2(INITIAL);
    timerFrame.jToolBar.add(jLabel);
    // geometricComponent().setOffset(300, 700);
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
    jLabel.setText("s1=" + clothoidContext.s1().maps(Round._4) + " s2=" + clothoidContext.s2().maps(Round._4));
    // ---
    {
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.of(clothoidContext.s1(), clothoidContext.s2())));
      graphics.setColor(Color.RED);
      graphics.fill(geometricLayer.toPath2D(CirclePoints.of(8).multiply(RealScalar.of(0.1))));
      geometricLayer.popMatrix();
    }
    graphics.setStroke(new BasicStroke(1.5f));
    for (Tensor _lambda : clothoidSolutions.lambdas()) {
      ClothoidBuilder clothoidBuilder = CustomClothoidBuilder.of((Scalar) _lambda);
      ClothoidTransition clothoidTransition = ClothoidTransition.of(clothoidBuilder, clothoidContext.p(), clothoidContext.q());
      Tensor points = clothoidTransition.linearized(MIN_RESOLUTION);
      new PathRender(new Color(64, 128, 64, 128 + 32)).setCurve(points, false).render(geometricLayer, graphics);
    }
    // ---
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    Show show = ClothoidTangentDefectShow.of(clothoidContext, clip).getShow();
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
    Dimension dimension = getSize();
    show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width, dimension.height / 2));
  }

  static void main() {
    new CustomClothoidDemo().runStandalone();
  }
}
