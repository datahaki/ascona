// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import javax.swing.JToggleButton;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.msh.MatrixArray;
import ch.alpine.ascony.msh.Meshgrid;
import ch.alpine.ascony.reg.RegionRenders;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.MeshRender;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.d2.ex.Box2D;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.noise.SimplexContinuousNoise;
import ch.alpine.sophis.var.InversePowerVariogram;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.rn.RGroup;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Clips;

/** transfer weights from barycentric coordinates defined by set of control points
 * in the square domain (subset of R^2) to means in non-linear spaces */
final class R2ScatteredSetCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  private static final double RANGE = 5;
  private final CoordinateBoundingBox cbb = Box2D.xy(Clips.absolute(RANGE));
  // ---
  private final JToggleButton jToggleAnimate = new JToggleButton("animate");
  private final Timing timing = Timing.started();
  // ---
  private Tensor snapshot;

  public R2ScatteredSetCoordinateDemo() {
    super(List.of(LogWeightings.values()));
    {
      jToggleAnimate.addActionListener(_ -> {
        if (jToggleAnimate.isSelected())
          snapshot = getControlPointsSe2();
        else
          setControlPointsSe2(snapshot);
      });
      jToolBar().add(jToggleAnimate);
    }
    geometricComponent().addRenderInterfaceBackground(RegionRenders.of(cbb));
    setControlPointsSe2(Tensors.fromString("{{2, -3, 1.5}, {3, 5, 1}, {-4, -3, 1}, {-5, 3, 2}}"));
    setControlPointsSe2(Tensors.fromString( //
        "{{-1.217, -2.050, 1.309}, {1.783, 1.917, 0.262}, {-3.583, 0.300, -0.262}, {2.200, -0.283, 0.262}, {-4.000, -3.000, 1.000}, {-1.900, 2.117, 1.309}}"));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_SE2;
  }

  private static Tensor random(double toc, int index) {
    return Tensors.vector( //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 0), //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 1), //
        SimplexContinuousNoise.FUNCTION.at(toc, index, 2) * 2);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ColorDataGradient colorDataGradient = scatteredSetParam.spinnerColorData;
    if (jToggleAnimate.isSelected()) {
      double toc = timing.seconds().multiply(Quantity.of(0.3, "s^-1")).number().doubleValue();
      int n = snapshot.length();
      Tensor control = Tensors.reserve(n);
      for (int index = 0; index < n; ++index) { //
        control.append(snapshot.get(index).add(random(toc, index)));
      }
      setControlPointsSe2(control);
    }
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor controlPoints = getGeodesicControlPoints();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    if (2 < controlPoints.length()) {
      Tensor domain = Tensor.of(controlPoints.stream().map(manifoldDisplay::point2xy));
      // ---
      // TODO inv pow var configurable!?
      Sedarim sedarim = Biinvariants.METRIC.ofSafe(RGroup.INSTANCE).coordinate(InversePowerVariogram.of(2), domain);
      Tensor weights = new Meshgrid(cbb, scatteredSetParam.refine).image(sedarim::sunder);
      Tensor fallback = manifoldDisplay.indetPoint();
      AveragedMovingDomain2D averagedMovingDomain2D = new AveragedMovingDomain2D(weights, homogeneousSpace.biinvariantMean(), fallback);
      Tensor[][] array = averagedMovingDomain2D.forward(controlPoints);
      Tensor[][] point = new MatrixArray(array).maps(manifoldDisplay.point2xy());
      new MeshRender(point, colorDataGradient.deriveWithOpacity(Rational.HALF)).render(geometricLayer, graphics);
      {
        Dimension dimension = getSize();
        Show show = new Show();
        show.add(ArrayPlot.of(averagedMovingDomain2D.arrayReshape_weights(), colorDataGradient));
        show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width, 300));
      }
      // render grid lines functions
      if (scatteredSetParam.arrows)
        manifoldDisplay.showPoints( //
            ColorPair.RMD, //
            RealScalar.of(Math.min(1, 3.0 / Math.sqrt(scatteredSetParam.refine))), //
            Tensor.of(Arrays.stream(array).flatMap(Arrays::stream))) //
            .render(geometricLayer, graphics);
    }
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, controlPoints, null, geometricLayer, graphics);
    leversRender.renderIndexP("q");
  }

  static void main() {
    new R2ScatteredSetCoordinateDemo().runStandalone();
  }
}
