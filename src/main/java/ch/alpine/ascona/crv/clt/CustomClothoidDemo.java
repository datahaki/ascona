// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JLabel;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidContext;
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
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Round;

public class CustomClothoidDemo extends ControlPointsDemo {
  private static final Tensor INITIAL = Tensors.fromString("{{0,0,0},{3,0,0}}");
  private static final Tensor POINTER = Tensors.fromString("{{0, 0}, {-0.2, -1}, {+0.2, -1}}");

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.CL_ONLY);
    }

    public Scalar lambda = RealScalar.ZERO;
    public Integer solution = 0;
  }

  private final JLabel jLabel = new JLabel();
  private static final Scalar MIN_RESOLUTION = RealScalar.of(0.05);
  // ---
  private final Param param;

  public CustomClothoidDemo() {
    this(new Param());
  }

  public CustomClothoidDemo(Param param) {
    super(param);
    this.param = param;
    setControlPointsSe2(INITIAL);
    timerFrame.jToolBar.add(jLabel);
    timerFrame.geometricComponent.setOffset(300, 700);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor p = getGeodesicControlPoints().get(0);
    Tensor q = getGeodesicControlPoints().get(1);
    ClothoidContext clothoidContext = new ClothoidContext(p, q);
    ClothoidTangentDefect clothoidTangentDefect = ClothoidTangentDefect.of(clothoidContext);
    ClothoidSolutions clothoidSolutions = new ClothoidSolutions(clothoidTangentDefect, Clips.absolute(15.0));
    ClothoidDefectContainer clothoidDefectContainer = new ClothoidDefectContainer(clothoidContext, clothoidSolutions);
    // ---
    jLabel.setText("s1=" + clothoidContext.s1().maps(Round._4) + " s2=" + clothoidContext.s2().maps(Round._4));
    // ---
    Scalar lambda = param.lambda;
    {
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.of(clothoidContext.s1(), clothoidContext.s2())));
      graphics.setColor(Color.RED);
      graphics.fill(geometricLayer.toPath2D(CirclePoints.of(8).multiply(RealScalar.of(0.1))));
      geometricLayer.popMatrix();
    }
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    GeometricLayer plotLayer = new GeometricLayer(Tensors.matrix(new Number[][] { //
        { 30, 0, dimension.width / 2 }, //
        { 0, -30, 200 }, //
        { 0, 0, 1 } }));
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
    {
      GridRender gridRender = new GridRender(Subdivide.of(-20, 20, 10), Subdivide.of(-3, 3, 6));
      gridRender.render(plotLayer, graphics);
      clothoidDefectContainer.render(plotLayer, graphics);
      graphics.setColor(Color.RED);
      plotLayer.pushMatrix(Se2Matrix.translation(Tensors.of(lambda, lambda.zero())));
      graphics.setStroke(new BasicStroke(2f));
      graphics.fill(plotLayer.toPath2D(POINTER, true));
      plotLayer.popMatrix();
      // ---
      Scalar s1 = clothoidContext.b0().add(clothoidContext.b1()).multiply(Rational.HALF);
      Scalar reifs = MidpointTangentApproximation.ORDER2.apply(clothoidContext.b0(), clothoidContext.b1()).subtract(s1);
      graphics.setColor(Color.CYAN);
      graphics.draw(plotLayer.toLine2D(Tensors.of(reifs, RealScalar.ZERO), Tensors.of(reifs, RealScalar.ONE.negate())));
      graphics.setStroke(new BasicStroke(1f));
    }
  }

  public void setLambda(Scalar lambda) {
    param.lambda = Round._6.apply(lambda);
    fieldsEditor(0).updateJComponents();
  }

  static void main() {
    new CustomClothoidDemo().runStandalone();
  }
}
