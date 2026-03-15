// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.math.Do;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.qty.Quantity;

class HermiteDatasetDemo extends AbstractHermiteDatasetDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  @ReflectionMarker
  static class Paran {
    @FieldSelectionArray({ "0", "2", "4", "6", "8", "10", "15", "20" })
    public Scalar shift = RealScalar.of(0);
    public HermiteSubdivisions scheme = HermiteSubdivisions.HERMITE3;
    @FieldSlider
    @FieldPreferredWidth(80)
    @FieldClip(min = "0", max = "8")
    public Integer level = 3;
    public Boolean diff = true;
    public final HermiteSubdivisionParam hsp = HermiteSubdivisionParam.GLOBAL;
  }

  private final Paran paran;

  public HermiteDatasetDemo() {
    super(paran = new Paran());
  }
  // protected void updateState() {
  // int limit = param.limit;
  // String name = param.string;
  // Tensor control = gokartPoseDataV2.getPoseVel(name, limit);
  // control.set(new So2Lift(), Tensor.ALL, 0, 2);
  // Tensor result = Tensors.empty();
  // int _skips = param.skips.number().intValue();
  // int offset = param.shift.number().intValue();
  // for (int index = offset; index < control.length(); index += _skips)
  // result.append(control.get(index));
  // // TensorUnaryOperator centerFilter = //
  // // CenterFilter.of(GeodesicCenter.of(Se2Geodesic.INSTANCE, GaussianWindow.FUNCTION), 4);
  // _control = result;
  // }

  @SuppressWarnings("unused")
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    new PathRender(ColorStroke.CURVE, _control.get(Tensor.ALL, 0), false).render(geometricLayer, graphics);
    if (_control.length() <= 1000)
      manifoldDisplay.showPoints(ColorPair.APPROXIMATION, RealScalar.ONE, _control.get(Tensor.ALL, 0)) //
          .render(geometricLayer, graphics);
    Scalar delta = getDelta();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    HermiteSubdivision hermiteSubdivision = paran.scheme.supply(homogeneousSpace);
    // IO.println(Dimensions.of(_control));
    TensorIteration tensorIteration = hermiteSubdivision.string(delta, _control);
    int levels = paran.level;
    Tensor refined = Do.of(_control, tensorIteration::iterate, levels);
    new PathRender(ColorStroke.SECONDARY_CURVE, refined.get(Tensor.ALL, 0), false).render(geometricLayer, graphics);
    if (getSelectedMD().equals(ManifoldDisplays.Se2))
      new Se2HermiteRender(refined, Quantity.of(0.3, "s")).render(geometricLayer, graphics);
    if (paran.diff) {
      Tensor deltas = refined.get(Tensor.ALL, 1);
      int dims = deltas.get(0).length();
      if (0 < deltas.length()) {
        Show show = StaticHelper.listPlot(deltas, delta, levels);
        Dimension dimension = getSize();
        show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
      }
    }
  }

  static void main() {
    new HermiteDatasetDemo().runStandalone();
  }
}
