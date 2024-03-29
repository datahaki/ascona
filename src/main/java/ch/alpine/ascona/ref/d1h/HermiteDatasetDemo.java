// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.api.HermiteSubdivisionParam;
import ch.alpine.ascona.util.api.HermiteSubdivisions;
import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dat.GokartPoseDatas;
import ch.alpine.ascona.util.dat.GokartPoseParam;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.so2.So2Lift;
import ch.alpine.sophus.math.Do;
import ch.alpine.sophus.math.api.TensorIteration;
import ch.alpine.sophus.ref.d1h.HermiteSubdivision;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

public class HermiteDatasetDemo extends AbstractDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);
  private static final Color COLOR_RECON = new Color(128, 128, 128, 255);
  // ---
  private static final Stroke STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private final PathRender pathRenderCurve = new PathRender(COLOR_CURVE, STROKE);
  private final PathRender pathRenderShape = new PathRender(COLOR_RECON, 2f);

  @ReflectionMarker
  public static class Param extends GokartPoseParam {
    public Param(GokartPoseData gokartPoseData) {
      super(gokartPoseData, ManifoldDisplays.SE2C_SE2);
    }

    @FieldSelectionArray({ "1", "2", "5", "10", "25", "50", "100", "250", "500" })
    public Scalar skips = RealScalar.of(50);
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

  private final GokartPoseDataV2 gokartPoseDataV2;
  protected Tensor _control = Tensors.empty();
  private final Param param;

  public HermiteDatasetDemo() {
    this(GokartPoseDataV2.RACING_DAY);
  }

  public HermiteDatasetDemo(GokartPoseDataV2 gokartPoseData) {
    this(new Param(gokartPoseData));
  }

  public HermiteDatasetDemo(Param param) {
    super(param);
    this.param = param;
    this.gokartPoseDataV2 = (GokartPoseDataV2) param.gpd();
    fieldsEditor(0).addUniversalListener(this::updateState);
    // ---
    timerFrame.geometricComponent.setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
    updateState();
  }

  protected void updateState() {
    int limit = param.limit;
    String name = param.string;
    Tensor control = gokartPoseDataV2.getPoseVel(name, limit);
    control.set(new So2Lift(), Tensor.ALL, 0, 2);
    Tensor result = Tensors.empty();
    int _skips = param.skips.number().intValue();
    int offset = param.shift.number().intValue();
    for (int index = offset; index < control.length(); index += _skips)
      result.append(control.get(index));
    // TensorUnaryOperator centerFilter = //
    // CenterFilter.of(GeodesicCenter.of(Se2Geodesic.INSTANCE, GaussianWindow.FUNCTION), 4);
    _control = result;
  }

  @SuppressWarnings("unused")
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = param.manifoldDisplays.manifoldDisplay();
    {
      final Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(1));
      pathRenderCurve.setCurve(_control.get(Tensor.ALL, 0), false).render(geometricLayer, graphics);
      if (_control.length() <= 1000)
        for (Tensor point : _control.get(Tensor.ALL, 0)) {
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
          Path2D path2d = geometricLayer.toPath2D(shape);
          path2d.closePath();
          graphics.setColor(new Color(255, 128, 128, 64));
          graphics.fill(path2d);
          graphics.setColor(COLOR_CURVE);
          graphics.draw(path2d);
          geometricLayer.popMatrix();
        }
    }
    graphics.setColor(Color.DARK_GRAY);
    Scalar delta = RationalScalar.of(param.skips.number().intValue(), 50);
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    HermiteSubdivision hermiteSubdivision = param.scheme.supply(homogeneousSpace);
    TensorIteration tensorIteration = hermiteSubdivision.string(delta, _control);
    int levels = param.level;
    Tensor refined = Do.of(_control, tensorIteration::iterate, levels);
    pathRenderShape.setCurve(refined.get(Tensor.ALL, 0), false).render(geometricLayer, graphics);
    new Se2HermiteRender(refined, RealScalar.of(0.3)).render(geometricLayer, graphics);
    if (param.diff) {
      Tensor deltas = refined.get(Tensor.ALL, 1);
      int dims = deltas.get(0).length();
      if (0 < deltas.length()) {
        Show show = StaticHelper.listPlot(deltas, delta, levels);
        Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
        show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
