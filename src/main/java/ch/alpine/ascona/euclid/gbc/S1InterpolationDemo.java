// code by jph
package ch.alpine.ascona.euclid.gbc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.reg.RegionRenders;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.Box2D;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.hs.s.SnManifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.lie.rot.AngleVector;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Clips;

public class S1InterpolationDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
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
    // ---
    setControlPointsSe2(Tensors.fromString("{{1, 0, 0}, {0, 1.2, 0}, {-0.5, 0.8, 0}}"));
    // geometricComponent().setOffset(500, 500);
    geometricComponent().addRenderInterfaceBackground(RegionRenders.of(coordinateBoundingBox));
    geometricComponent().addRenderInterfaceBackground(S1FrameRender.INSTANCE);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = SnManifold.INSTANCE;
    Tensor control = Tensor.of(getGeodesicControlPoints().stream() //
        .filter(v -> Scalars.nonZero(Vector2Norm.of(v))));
    if (0 < control.length()) {
      Tensor sequence = Vector2Norm.NORMALIZE.slash(control);
      Tensor target = sequence;
      graphics.setColor(Color.GREEN);
      for (int index = 0; index < target.length(); ++index)
        graphics.draw(geometricLayer.toLine2D(control.get(index), target.get(index)));
      manifoldDisplay.showPoints(ColorPair.S1I, RealScalar.ONE, target) //
          .render(geometricLayer, graphics);
      // ---
      Tensor values = Tensor.of(control.stream().map(Vector2Norm::of));
      Tensor domain = Subdivide.of(Pi.VALUE.negate(), Pi.VALUE, 511);
      Tensor spherics = domain.maps(AngleVector::of);
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

  static void main() {
    new S1InterpolationDemo().runStandalone();
  }
}
