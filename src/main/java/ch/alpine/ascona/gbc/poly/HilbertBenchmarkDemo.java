// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.crv.d2.HilbertPolygon;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.pow.Power;

/** References:
 * "Iterative coordinates"
 * by Chongyang Deng, Qingjun Chang, Kai Hormann, 2020 */
public class HilbertBenchmarkDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldInteger
    @FieldClip(min = "1", max = "4")
    public Scalar levels = RealScalar.of(2);
    @FieldInteger
    @FieldClip(min = "20", max = "100")
    @FieldSelectionArray({ "30", "50" })
    public Scalar resolution = RealScalar.of(20);
    public Boolean ctrl = false;
  }

  private final Param param;
  private Tensor polygon;

  public HilbertBenchmarkDemo() {
    this(new Param());
  }

  public HilbertBenchmarkDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor.addUniversalListener(this::updateCtrl);
    // ---
    updateCtrl();
  }

  void updateCtrl() {
    System.out.println("update");
    polygon = unit(param.levels.number().intValue());
    bufferedImage = null;
  }

  private BufferedImage bufferedImage = null;

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
    int magnification = 4;
    if (Objects.isNull(bufferedImage))
      compute();
    if (Objects.nonNull(bufferedImage))
      graphics.drawImage(bufferedImage, 0, 200, bufferedImage.getWidth() * magnification, bufferedImage.getHeight() * magnification, null);
  }

  public void compute() {
    bufferedImage = HilbertLevelImage.of(R2Display.INSTANCE, polygon, param.resolution.number().intValue(), ColorDataGradients.CLASSIC, 32);
  }

  /** @param n positive
   * @return hilbert polygon inside unit square [0, 1]^2 */
  public static Tensor unit(int n) {
    Tensor polygon = HilbertPolygon.of(n).multiply(Power.of(2.0, -n + 1));
    return polygon.map(scalar -> scalar.subtract(RealScalar.of(1.0 + 1e-5)));
  }

  public static void main(String[] args) {
    launch();
  }
}
