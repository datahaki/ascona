// code by jph
package ch.alpine.euclid.hil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

import ch.alpine.ascony.api.Box2D;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.HilbertPolygon;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.pow.Power;

/** References:
 * "Iterative coordinates"
 * by Chongyang Deng, Qingjun Chang, Kai Hormann, 2020 */
public class HilbertBenchmarkDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldClip(min = "1", max = "4")
    public Integer levels = 2;
    @FieldClip(min = "20", max = "100")
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer resolution = 20;
    public Boolean ctrl = false;
    public ColorDataGradients cdg = ColorDataGradients.CLASSIC;
  }

  private final Param param;
  private Tensor polygon;

  public HilbertBenchmarkDemo() {
    this(new Param());
  }

  public HilbertBenchmarkDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(this::updateCtrl);
    // ---
    updateCtrl();
  }

  void updateCtrl() {
    polygon = unit(param.levels);
    show = null;
  }

  private Show show = null;

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = R2Display.INSTANCE;
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.CORNERS, true));
    // ---
    RenderQuality.setQuality(graphics);
    final Tensor sequence = polygon;
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
    if (param.ctrl) {
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    leversRender.renderSurfaceP();
    if (Objects.isNull(show))
      compute();
    if (Objects.nonNull(show))
      show.render(graphics, new Rectangle(100, 10, 400, 300));
  }

  public void compute() {
    show = HilbertLevelShow.of(polygon, param.resolution, param.cdg, 32);
  }

  /** @param n positive
   * @return hilbert polygon inside unit square [0, 1]^2 */
  public static Tensor unit(int n) {
    Tensor polygon = HilbertPolygon.of(n).multiply(Power.of(2.0, -n + 1));
    return polygon.maps(scalar -> scalar.subtract(RealScalar.of(1.0 + 1e-5)));
  }

  static void main() {
    new HilbertBenchmarkDemo().runStandalone();
  }
}
