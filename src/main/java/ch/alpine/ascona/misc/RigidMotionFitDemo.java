// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.fit.Se2RigidMotionFit;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.sophus.lie.se2.Se2ForwardAction;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.sca.Clips;

// TODO ASCONA REV enhance plot with origin and path
// ... also use p_i, and q_i
public class RigidMotionFitDemo extends ControlPointsDemo {
  private static final Tensor ORIGIN = CirclePoints.of(3).multiply(RealScalar.of(0.2));
  private static final PointsRender POINTS_RENDER_RESULT = //
      new PointsRender(new Color(128, 128, 255, 64), new Color(128, 128, 255, 255));
  private static final PointsRender POINTS_RENDER_POINTS = //
      new PointsRender(new Color(64, 255, 64, 64), new Color(64, 255, 64, 255));

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.R2_ONLY);
    }

    @FieldClip(min = "2", max = "10")
    public Integer length = 5;
  }

  private final Param param;
  private Tensor points;

  public RigidMotionFitDemo() {
    this(new Param());
  }

  public RigidMotionFitDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    fieldsEditor(0).addUniversalListener(this::shufflePoints);
    // ---
    shufflePoints();
  }

  private synchronized void shufflePoints() {
    int n = param.length;
    Distribution distribution = NormalDistribution.of(0, 2);
    points = RandomVariate.of(distribution, n, 2);
    Tensor xya = RandomVariate.of(distribution, 3);
    setControlPointsSe2(Tensor.of(points.stream() //
        .map(new Se2ForwardAction(xya)) //
        .map(row -> row.append(RealScalar.ZERO))));
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor sequence = getGeodesicControlPoints();
    {
      Tensor target = Tensor.of(sequence.stream().map(R2Display.INSTANCE::xya2point));
      Tensor solve = Se2RigidMotionFit.of(points, target);
      POINTS_RENDER_RESULT //
          .show(Se2Display.INSTANCE::matrixLift, Se2Display.INSTANCE.shape(), Tensors.of(solve)) //
          .render(geometricLayer, graphics);
      {
        Tensor domain = Subdivide.increasing(Clips.unit(), 10);
        // LieGroupElement lieGroupElement =
        for (Tensor p : points) {
          Tensor xya_0 = Append.of(p, RealScalar.ZERO);
          Tensor xya_1 = Se2CoveringGroup.INSTANCE.combine(solve, xya_0);
          ScalarTensorFunction scalarTensorFunction = Se2CoveringGroup.INSTANCE.curve(xya_0, xya_1);
          Tensor tensor = domain.map(scalarTensorFunction);
          new PathRender(Color.CYAN, 1.5f).setCurve(tensor, false).render(geometricLayer, graphics);
        }
      }
      graphics.setColor(Color.RED);
      for (int index = 0; index < points.length(); ++index)
        graphics.draw(geometricLayer.toLine2D(points.get(index), target.get(index)));
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay(), sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
    POINTS_RENDER_POINTS //
        .show(R2Display.INSTANCE::matrixLift, ORIGIN, points) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    launch();
  }
}
