// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.SnLineDistances;
import ch.alpine.ascona.util.arp.S2ArrayHelper;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldLabel;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorNorm;
import ch.alpine.sophus.hs.VectorLogManifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.red.Times;

@ReflectionMarker
public class S2LineDistanceDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Tensor GEODESIC_DOMAIN = Subdivide.of(0.0, 1.0, 11);
  private static final Tensor INITIAL = Tensors.fromString("{{-0.5, 0, 0}, {0.5, 0, 0}}").unmodifiable();

  // ---
  public static class Param {
    @FieldLabel("S^n line distance method")
    public SnLineDistances snLineDistances = SnLineDistances.DEFAULT;
    @FieldInteger
    @FieldSelectionArray(value = { "20", "30", "50", "75", "100", "150", "200", "250" })
    public Scalar resolution = RealScalar.of(30);
    @FieldLabel("color data gradient")
    public ColorDataGradients colorDataGradients = ColorDataGradients.PARULA;
  }

  public final Param param = new Param();

  public S2LineDistanceDemo() {
    super(false, ManifoldDisplays.S2_ONLY);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    setControlPointsSe2(INITIAL);
    // ---
    Tensor model2pixel = timerFrame.geometricComponent.getModel2Pixel();
    timerFrame.geometricComponent.setModel2Pixel(Times.of(Tensors.vector(5, 5, 1), model2pixel));
    // ---
    timerFrame.geometricComponent.setOffset(400, 400);
    setMidpointIndicated(false);
  }

  TensorNorm tensorNorm() {
    Tensor cp = getGeodesicControlPoints();
    return 1 < cp.length() //
        ? param.snLineDistances.lineDistance().tensorNorm(cp.get(0), cp.get(1))
        : t -> RealScalar.ZERO;
  }

  private BufferedImage bufferedImage(int resolution, VectorLogManifold vectorLogManifold) {
    Tensor matrix = Tensors.matrix(S2ArrayHelper.of(resolution, rad(), tensorNorm()::norm));
    return ImageFormat.of(matrix.map(param.colorDataGradients));
  }

  double rad() {
    return 1;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setDefault(graphics);
    BufferedImage bufferedImage = bufferedImage(param.resolution.number().intValue(), manifoldDisplay.hsManifold());
    ImageRender.of(bufferedImage, S2ArrayHelper.pixel2model(bufferedImage, rad())) //
        .render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    // ---
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor cp = getGeodesicControlPoints();
    ScalarTensorFunction scalarTensorFunction = geodesicSpace.curve(cp.get(0), cp.get(1));
    graphics.setStroke(STROKE);
    Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(manifoldDisplay::toPoint));
    graphics.setColor(new Color(192, 192, 192));
    graphics.draw(geometricLayer.toPath2D(ms));
    graphics.setStroke(new BasicStroke());
    // ---
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, cp, null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.DARK.updateUI();
    new S2LineDistanceDemo().setVisible(1200, 600);
  }
}
