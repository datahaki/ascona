// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.BoundingBoxRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.hs.sn.SnManifold;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.lie.r2.AngleVector;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Clips;

public class S1InterpolationDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    @FieldSelectionCallback("logWeightings")
    public LogWeightings logWeightings = LogWeightings.WEIGHTING;
    @FieldSelectionCallback("biinvariants")
    public Biinvariants biinvariants = Biinvariants.METRIC;

    @ReflectionMarker
    public List<LogWeightings> logWeightings() {
      return LogWeightings.noDistances();
    }

    @ReflectionMarker
    public List<Biinvariants> biinvariants() {
      return Biinvariants.FAST;
    }
  }

  private static final double RANGE = 2;
  private final CoordinateBoundingBox coordinateBoundingBox = Box2D.xy(Clips.absolute(RANGE));
  private final Param param;

  public S1InterpolationDemo() {
    this(new Param());
  }

  public S1InterpolationDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{1, 0, 0}, {0, 1.2, 0}, {-0.5, 0.8, 0}}"));
    timerFrame.geometricComponent.setOffset(500, 500);
    timerFrame.geometricComponent.addRenderInterfaceBackground(new BoundingBoxRender(coordinateBoundingBox));
    timerFrame.geometricComponent.addRenderInterfaceBackground(S1FrameRender.INSTANCE);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = SnManifold.INSTANCE;
    Tensor control = getGeodesicControlPoints();
    final Tensor shape = manifoldDisplay.shape(); // .multiply(RealScalar.of(0.3));
    if (0 < control.length()) {
      // TODO ASCONA ALG check for zero norm below
      Tensor sequence = Tensor.of(control.stream().map(Vector2Norm.NORMALIZE));
      Tensor target = sequence;
      graphics.setColor(Color.GREEN);
      for (int index = 0; index < target.length(); ++index)
        graphics.draw(geometricLayer.toLine2D(control.get(index), target.get(index)));
      new PointsRender(new Color(64, 128, 64, 64), new Color(64, 128, 64, 255))
          // new PointsRender(new Color(128, 255, 128, 64), new Color(128, 255, 128, 255)) //
          .show(manifoldDisplay::matrixLift, shape, target) //
          .render(geometricLayer, graphics);
      // ---
      Tensor values = Tensor.of(control.stream().map(Vector2Norm::of));
      Tensor domain = Subdivide.of(Pi.VALUE.negate(), Pi.VALUE, 511);
      Tensor spherics = domain.map(AngleVector::of);
      // ---
      ScalarUnaryOperator suo = param.logWeightings.variogramForInterpolation();
      if (param.logWeightings.forceMetric() && //
          !param.biinvariants.equals(Biinvariants.METRIC)) {
        param.biinvariants = Biinvariants.METRIC;
        fieldsEditor(0).updateJComponents();
      }
      Sedarim sedarim = param.logWeightings.sedarim(param.biinvariants.ofSafe(manifold), suo, sequence);
      try {
        ScalarTensorFunction scalarTensorFunction = //
            point -> sedarim.sunder(AngleVector.of(point));
        Tensor basis = Tensor.of(domain.stream().parallel().map(Scalar.class::cast).map(scalarTensorFunction));
        Tensor curve = Times.of(basis.dot(values), spherics);
        new PathRender(Color.BLUE, 1.25f).setCurve(curve, true).render(geometricLayer, graphics);
        // ---
        Reverse.of(spherics).forEach(curve::append);
        graphics.setColor(new Color(0, 0, 255, 32));
        graphics.fill(geometricLayer.toPath2D(curve));
      } catch (Exception exception) {
        // ---
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    // POINTS_RENDER_0.show(geodesicDisplay()::matrixLift, shape, getGeodesicControlPoints()).render(geometricLayer, graphics);
  }

  public static void main(String[] args) {
    launch();
  }
}
