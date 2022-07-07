// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dat.GokartPoseDatas;
import ch.alpine.ascona.util.dat.GokartPoseParam;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.lie.se2.Se2BiinvariantMeans;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.sophus.math.Do;
import ch.alpine.sophus.math.api.TensorIteration;
import ch.alpine.sophus.ref.d1h.Hermite3Filter;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

public class HermiteDatasetFilterDemo extends AbstractDemo {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);
  private static final Color COLOR_RECON = new Color(128, 128, 128, 255);
  // ---
  private final PathRender pathRenderCurve = new PathRender(COLOR_CURVE);
  private final PathRender pathRenderShape = new PathRender(COLOR_RECON, 2f);
  // ---
  private final GokartPoseDataV2 gokartPoseDataV2;

  @ReflectionMarker
  public static class Param extends GokartPoseParam {
    public Param(GokartPoseData gokartPoseData) {
      super(gokartPoseData, ManifoldDisplays.SE2_ONLY);
    }

    @FieldInteger
    @FieldSelectionArray({ "1", "2", "5", "10", "25", "50" })
    public Scalar skips = RealScalar.of(5);
    @FieldInteger
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6" })
    public Scalar level = RealScalar.of(5);
    public Boolean adjoint = false;
    public Boolean derivat = true;
  }

  private final Param param;
  protected Tensor _control = Tensors.empty();

  public HermiteDatasetFilterDemo() {
    this(GokartPoseDataV2.RACING_DAY);
  }

  public HermiteDatasetFilterDemo(GokartPoseDataV2 gokartPoseData) {
    this(new Param(gokartPoseData));
  }

  public HermiteDatasetFilterDemo(Param param) {
    super(param);
    this.param = param;
    this.gokartPoseDataV2 = (GokartPoseDataV2) param.gpd();
    timerFrame.geometricComponent.setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
    updateState();
  }

  protected void updateState() {
    int limit = param.limit.number().intValue();
    String name = param.string;
    Tensor control = gokartPoseDataV2.getPoseVel(name, limit);
    Tensor result = Tensors.empty();
    int skips = param.skips.number().intValue();
    for (int index = 0; index < control.length(); index += skips)
      result.append(control.get(index));
    _control = result;
  }

  @SuppressWarnings("unused")
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = param.manifoldDisplays.manifoldDisplay();
    {
      final Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.3));
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
    TensorIteration tensorIteration = //
        // new Hermite1Filter(Se2Group.INSTANCE, Se2CoveringExponential.INSTANCE).string(delta, _control);
        new Hermite3Filter(Se2Group.INSTANCE, Se2BiinvariantMeans.FILTER) //
            .string(delta, _control);
    int levels = 2 * param.level.number().intValue();
    Tensor refined = Do.of(_control, tensorIteration::iterate, levels);
    {
      final Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.3));
      for (Tensor point : refined.get(Tensor.ALL, 0)) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
        Path2D path2d = geometricLayer.toPath2D(shape);
        path2d.closePath();
        graphics.setColor(new Color(128, 255, 128, 64));
        graphics.fill(path2d);
        graphics.setColor(COLOR_CURVE);
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    pathRenderShape.setCurve(refined.get(Tensor.ALL, 0), false).render(geometricLayer, graphics);
    if (param.derivat) {
      Tensor deltas = refined.get(Tensor.ALL, 1);
      int dims = deltas.get(0).length();
      if (0 < deltas.length()) {
        JFreeChart jFreeChart = StaticHelper.listPlot(deltas, delta, levels);
        Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
        jFreeChart.draw(graphics, new Rectangle2D.Double(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
      }
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new HermiteDatasetFilterDemo(GokartPoseDataV2.RACING_DAY).setVisible(1000, 800);
  }
}
