// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.api.HermiteSubdivisionParam;
import ch.alpine.ascony.api.HermiteSubdivisions;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.ClothoidDistance;
import ch.alpine.sophis.crv.d2.Extract2D;
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
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.lie.rot.AngleVector;
import ch.alpine.tensor.red.Mean;

public class HermiteSubdivisionDemo extends ControlPointsDemo {
  // TODO ASCONA redundant
  private static final PointsRender POINTS_RENDER_0 = //
      new PointsRender(new Color(255, 128, 128, 64), new Color(255, 128, 128, 255));
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  // ---
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.SE2C_SE2_R2);
    }

    public HermiteSubdivisions scheme = HermiteSubdivisions.HERMITE3;
    public final HermiteSubdivisionParam hsp = HermiteSubdivisionParam.GLOBAL;
    @FieldSlider
    @FieldClip(min = "0", max = "7")
    public Integer refine = 6;
    public Boolean diff = true;
  }

  private final Param param;

  public HermiteSubdivisionDemo() {
    this(new Param());
  }

  public HermiteSubdivisionDemo(Param param) {
    super(param);
    this.param = param;
  }

  private static final GridRender GRID_RENDER = new GridRender(Subdivide.of(0, 10, 10));

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    GRID_RENDER.render(geometricLayer, graphics);
    final Tensor tensor = getControlPointsSe2();
    POINTS_RENDER_0.show(Se2Display.INSTANCE::matrixLift, //
        Se2Display.INSTANCE.shape(), //
        tensor).render(geometricLayer, graphics);
    // renderControlPoints(geometricLayer, graphics);
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
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      HermiteSubdivision hermiteSubdivision = param.scheme.supply(homogeneousSpace);
      TensorIteration tensorIteration = hermiteSubdivision.string(delta, control);
      int levels = param.refine;
      Tensor iterate = Do.of(control, tensorIteration::iterate, levels);
      Tensor curve = Tensor.of(iterate.get(Tensor.ALL, 0).stream().map(Extract2D.FUNCTION));
      Curvature2DRender.of(curve, false).render(geometricLayer, graphics);
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
          Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
          show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
        }
      }
    }
  }

  static void main() {
    launch();
  }
}
