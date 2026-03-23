// code by jph
package ch.alpine.ascona.decim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorStrokeIndexed;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.LineDistances;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.pdf.RandomSample;

/** playground for curve decimation */
class DecimationDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    public Integer n = 5;
    public Scalar epsilon = RealScalar.ONE;
  }

  private final Param param;

  public DecimationDemo() {
    super(param = new Param());
    fieldsEditor(param).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    setGeodesicControlPoints(RandomSample.of(manifoldDisplay().randomSampleInterface(), param.n));
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_R2_S2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    int length = sequence.length();
    if (0 == length)
      return;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    graphics.setColor(Color.LIGHT_GRAY);
    Tensor domain = Subdivide.of(0.0, 1.0, 10);
    {
      for (int index = 1; index < sequence.length(); ++index) {
        Tensor tensor = domain.maps(homogeneousSpace.curve(sequence.get(index - 1), sequence.get(index)));
        new PathRender(ColorStrokeIndexed._097.getColorStroke(0), tensor, false) //
            .render(geometricLayer, graphics);
      }
    }
    CurveDecimation curveDecimation = CurveDecimation.of( //
        LineDistances.STANDARD.supply(homogeneousSpace), param.epsilon);
    Tensor decimate = curveDecimation.apply(sequence);
    {
      for (int index = 1; index < decimate.length(); ++index) {
        Tensor tensor = domain.maps(homogeneousSpace.curve(decimate.get(index - 1), decimate.get(index)));
        new PathRender(ColorStrokeIndexed._097.getColorStroke(1), tensor, false) //
            .render(geometricLayer, graphics);
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new DecimationDemo().runStandalone();
  }
}
