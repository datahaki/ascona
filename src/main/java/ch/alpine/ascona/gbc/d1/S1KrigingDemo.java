// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.BoundingBoxRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.hs.r2.ArcTan2D;
import ch.alpine.sophus.itp.Kriging;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Drop;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;

// FIXME ASCONA DEMO what does this demo do: there is no curve shown
public class S1KrigingDemo extends ControlPointsDemo {
  private static final double RANGE = 2;
  private static final Tensor DOMAIN = Drop.tail(CirclePoints.of(161).map(N.DOUBLE), 80);
  private static final CoordinateBoundingBox coordinateBoundingBox = Box2D.xy(Clips.absolute(RANGE));

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public LogWeightings logWeightings = LogWeightings.KRIGING;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    public Boolean type = false;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 40;
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  public S1KrigingDemo() {
    this(new Param());
  }

  public S1KrigingDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{1, 0, 0}, {0, 1.2, 0}, {-1, 1, 0}}"));
    timerFrame.geometricComponent.addRenderInterfaceBackground(new BoundingBoxRender(coordinateBoundingBox));
    timerFrame.geometricComponent.addRenderInterfaceBackground(S1FrameRender.INSTANCE);
    timerFrame.geometricComponent.setOffset(500, 500);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    final Tensor shape = manifoldDisplay.shape(); // .multiply(RealScalar.of(0.3));
    if (1 < control.length()) {
      // TODO ASCONA ALG check for zero norm below
      Tensor sequence = Tensor.of(control.stream().map(Vector2Norm.NORMALIZE));
      Tensor funceva = Tensor.of(control.stream().map(Vector2Norm::of));
      Tensor cvarian = getControlPointsSe2().get(Tensor.ALL, 2).multiply(RationalScalar.HALF).map(Abs.FUNCTION);
      // ---
      graphics.setColor(new Color(0, 128, 128));
      Scalar IND = RealScalar.of(0.1);
      for (int index = 0; index < sequence.length(); ++index) {
        Tensor xy = control.get(index).copy();
        xy.append(ArcTan2D.of(xy).add(Pi.HALF));
        geometricLayer.pushMatrix(GfxMatrix.of(xy));
        Scalar v = cvarian.Get(index);
        graphics.draw(geometricLayer.toLine2D(Tensors.of(v.zero(), v), Tensors.of(v.zero(), v.negate())));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(IND, v), Tensors.of(IND.negate(), v)));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(IND, v.negate()), Tensors.of(IND.negate(), v.negate())));
        geometricLayer.popMatrix();
      }
      // ---
      graphics.setColor(Color.GREEN);
      for (int index = 0; index < sequence.length(); ++index)
        graphics.draw(geometricLayer.toLine2D(control.get(index), sequence.get(index)));
      new PointsRender(new Color(64, 128, 64, 64), new Color(64, 128, 64, 255)) //
          .show(manifoldDisplay()::matrixLift, shape, sequence) //
          .render(geometricLayer, graphics);
      Tensor covariance = DiagonalMatrix.with(cvarian);
      // if (isDeterminate())
      {
        Sedarim sedarim = param.logWeightings.sedarim(param.biinvariants.ofSafe(manifold), s -> s, sequence);
        Kriging kriging = Kriging.regression(sedarim, sequence, funceva, covariance);
        Tensor estimate = Tensor.of(DOMAIN.stream().map(kriging::estimate));
        Tensor curve = Times.of(estimate, DOMAIN);
        new PathRender(Color.BLUE, 1.25f).setCurve(curve, false).render(geometricLayer, graphics);
        Tensor errors = Tensor.of(DOMAIN.stream().map(kriging::variance));
        // ---
        Path2D path2d = geometricLayer.toPath2D(Join.of( //
            Times.of(estimate.add(errors), DOMAIN), //
            Reverse.of(Times.of(estimate.subtract(errors), DOMAIN))));
        graphics.setColor(new Color(128, 128, 128, 32));
        graphics.fill(path2d);
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
