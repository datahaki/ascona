// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
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
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.sca.Clips;

class RigidMotionFitDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    @FieldClip(min = "2", max = "10")
    public Integer length = 5;
  }

  private final Param param;
  private Tensor points;

  public RigidMotionFitDemo() {
    super(param = new Param());
    // ---
    fieldsEditor(param).addUniversalListener(this::shufflePoints);
    // ---
    shufflePoints();
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
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
    Tensor sequence = getGeodesicControlPoints();
    {
      Tensor target = Tensor.of(sequence.stream().map(R2Display.INSTANCE::xya2point));
      Tensor solve = Se2RigidMotionFit.of(points, target);
      Se2Display.INSTANCE.showPoints(ColorPair.ORIGIN, RealScalar.ONE, Tensors.of(solve)) //
          .render(geometricLayer, graphics);
      {
        Tensor domain = Subdivide.increasing(Clips.unit(), 10);
        for (Tensor p : points) {
          Tensor xya_0 = Append.of(p, RealScalar.ZERO);
          Tensor xya_1 = Se2CoveringGroup.INSTANCE.combine(solve, xya_0);
          ScalarTensorFunction scalarTensorFunction = Se2CoveringGroup.INSTANCE.curve(xya_0, xya_1);
          Tensor tensor = domain.maps(scalarTensorFunction);
          new PathRender(ColorStroke.CURVE, tensor, false).render(geometricLayer, graphics);
        }
      }
      graphics.setColor(Color.RED);
      for (int index = 0; index < points.length(); ++index)
        graphics.draw(geometricLayer.toLine2D(points.get(index), target.get(index)));
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    manifoldDisplay.showPoints(ColorPair.ORIGIN, RealScalar.of(0.8), Array.zeros(1, 2)) //
        .render(geometricLayer, graphics);
    manifoldDisplay.showPoints(ColorPair.REFERENCE, RealScalar.of(0.9), points) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    new RigidMotionFitDemo().runStandalone();
  }
}
