// code by jph
package ch.alpine.ascona.crv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.List;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;

public class BarycentricExtrapolationDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);

  @ReflectionMarker
  public static class Param {
    public LogWeightings logWeightings = LogWeightings.LAGRAINATE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
  }

  private final Param param;

  public BarycentricExtrapolationDemo() {
    this(new Param());
  }

  public BarycentricExtrapolationDemo(Param param) {
    super(param);
    this.param = param;
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_R2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    Tensor sequence = getGeodesicControlPoints();
    int length = sequence.length();
    Tensor domain = Range.of(-sequence.length(), 0).maps(Tensors::of).unmodifiable();
    graphics.setColor(Color.GRAY);
    graphics.setStroke(STROKE);
    for (int index = 0; index < length; ++index) {
      Line2D line2d = geometricLayer.toLine2D( //
          domain.get(index).append(RealScalar.ZERO), //
          manifoldDisplay.point2xy(sequence.get(index)));
      graphics.draw(line2d);
    }
    graphics.setStroke(new BasicStroke());
    if (1 < length) {
      Tensor samples = Subdivide.of(-length, 0, 127).maps(Tensors::of);
      Sedarim sedarim = param.logWeightings.sedarim(param.biinvariants.ofSafe(homogeneousSpace), s -> s, domain);
      Tensor curve = Tensor.of(samples.stream() //
          .map(sedarim::sunder) //
          .map(weights -> homogeneousSpace.biinvariantMean().mean(sequence, weights)));
      new PathRender(Color.BLUE, 1.5f) //
          .setCurve(curve, false) //
          .render(geometricLayer, graphics);
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new BarycentricExtrapolationDemo().runStandalone();
  }
}
