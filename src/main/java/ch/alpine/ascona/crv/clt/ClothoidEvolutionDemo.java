// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidComparators;
import ch.alpine.sophis.crv.clt.PriorityClothoid;
import ch.alpine.sophis.noise.SimplexContinuousNoise;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

class ClothoidEvolutionDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "0.05", "0.1", "0.2", "0.3", "0.4", "0.5" })
    public Scalar beta = RealScalar.of(0.05);
    public Boolean animate = true;
    public Clip clip = Clips.absolute(15.0);
  }

  private final Timing timing = Timing.started();
  public final Param param;

  public ClothoidEvolutionDemo() {
    this(new Param());
  }

  public ClothoidEvolutionDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    geometricComponent().addRenderInterfaceBackground(AxesRender.INSTANCE);
    // ---
    Tensor ctrl = Tensors.fromString( //
        "{{0.017, 0.017, 0.000}, {1.733, 0.967, 4.712}, {3.933, -0.750, -3.665}, {5.567, 1.717, 3.927}, {7.983, 1.500, 4.451}}");
    setControlPointsSe2(ctrl);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.CLC_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    graphics.setColor(Color.BLUE);
    graphics.setStroke(new BasicStroke(2));
    // GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    // ClothoidBuilder clothoidBuilder = (ClothoidBuilder) geodesicSpace;
    Tensor beg = sequence.get(0);
    ClothoidBuilder clothoidBuilder2 = new PriorityClothoid(ClothoidComparators.CURVATURE_HEAD, param.clip);
    double time = param.animate //
        ? timing.seconds().multiply(Quantity.of(0.2, "s^-1")).number().doubleValue()
        : 0;
    for (int index = 1; index < sequence.length(); ++index) {
      Tensor end = sequence.get(index);
      Clothoid clothoid = clothoidBuilder2.curve(beg, end);
      ClothoidTransition clothoidTransition = ClothoidTransition.of(beg, end, clothoid);
      graphics.draw(geometricLayer.toPath2D(clothoidTransition.linearized(param.beta)));
      double split = 0.5 + 0.1 * SimplexContinuousNoise.FUNCTION.at(time, index);
      beg = clothoidTransition.clothoid().apply(RealScalar.of(split));
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new ClothoidEvolutionDemo().runStandalone();
  }
}
