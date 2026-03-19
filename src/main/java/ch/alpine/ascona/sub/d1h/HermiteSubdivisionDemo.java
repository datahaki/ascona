// code by jph
package ch.alpine.ascona.sub.d1h;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.ClothoidDistance;
import ch.alpine.sophis.itp.AdjacentDistances;
import ch.alpine.sophis.math.Do;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.lie.rot.AngleVector;
import ch.alpine.tensor.red.Mean;

// TODO display in R2 does not seem correct
class HermiteSubdivisionDemo extends ControlPointsDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  // ---
  @ReflectionMarker
  static class Param {
    public HermiteSubdivisions scheme = HermiteSubdivisions.HERMITE3;
    public final HermiteSubdivisionParam hsp = HermiteSubdivisionParam.GLOBAL;
    @FieldSlider
    @FieldClip(min = "0", max = "7")
    public Integer refine = 6;
    public Boolean diff = true;
  }

  private final Param param;

  public HermiteSubdivisionDemo() {
    super(this.param = new Param());
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_SE2_R2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final Tensor tensor = getControlPointsSe2();
    Se2Display.INSTANCE.showPoints(ColorPair.DEC, RealScalar.ONE, tensor) //
        .render(geometricLayer, graphics);
    if (1 < tensor.length()) {
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      Tensor control;
      switch (manifoldDisplay.toString()) {
      case "SE2C":
      case "SE2":
        // TODO ASCONA ALG use various options: unit vector, scaled by parametric distance, ...
        control = Tensor.of(tensor.stream().map(xya -> Tensors.of(xya, UnitVector.of(3, 0))));
        break;
      case "R2":
        // TODO ASCONA ALG use various options: unit vector, scaled by parametric distance, ...
        control = Tensor.of(tensor.stream().map(xya -> Tensors.of(xya.extract(0, 2), AngleVector.of(xya.Get(2)))));
        break;
      default:
        return;
      }
      {
        Tensor distances = AdjacentDistances.of(ClothoidDistance.SE2_ANALYTIC).apply(tensor);
        // Distances.of(geodesicDisplay::parametricDistance, control.get(Tensor.ALL, 0));
        if (0 < distances.length()) {
          Tensor scaling = Array.zeros(control.length());
          scaling.set(distances.get(0), 0);
          for (int index = 1; index < distances.length(); ++index)
            scaling.set((Scalar) Mean.of(distances.extract(index - 1, index + 1)), index);
          scaling.set((Scalar) Last.of(distances), control.length() - 1);
          // ---
          for (int index = 0; index < control.length(); ++index) {
            int fi = index;
            control.set(t -> t.multiply(scaling.Get(fi)), index, 1);
          }
        }
      }
      Scalar delta = RealScalar.ONE;
      HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
      HermiteSubdivision hermiteSubdivision = param.scheme.supply(homogeneousSpace);
      TensorIteration tensorIteration = hermiteSubdivision.string(delta, control);
      int levels = param.refine;
      Tensor iterate = Do.of(control, tensorIteration::iterate, levels);
      Tensor positions = iterate.get(Tensor.ALL, 0);
      Tensor euclidXY = manifoldDisplay.point2xy().slash(positions);
      Curvature2DRender.of(euclidXY, false).render(geometricLayer, graphics);
      new PathRender(ColorStroke.CURVE, euclidXY, false).render(geometricLayer, graphics);
      {
        Scalar scale = RealScalar.of(0.3);
        switch (manifoldDisplay.toString()) {
        case "SE2C":
        case "SE2":
          new Se2HermiteRender(iterate, scale).render(geometricLayer, graphics);
          break;
        case "R2":
          new R2HermiteRender(iterate, scale).render(geometricLayer, graphics);
          break;
        default:
        }
      }
      // ---
      if (param.diff) {
        Tensor deltas = iterate.get(Tensor.ALL, 1);
        if (0 < deltas.length()) {
          Show show = StaticHelper.listPlot(deltas, delta, levels);
          Dimension dimension = getSize();
          show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
        }
      }
    }
  }

  static void main() {
    new HermiteSubdivisionDemo().runStandalone();
  }
}
