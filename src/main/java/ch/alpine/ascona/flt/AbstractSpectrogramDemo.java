// code by ob, jph
package ch.alpine.ascona.flt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.dat.GokartPoseDatas;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.GridRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Spectrogram;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.qty.QuantityMagnitude;

// @ReflectionMarker
/* package */ abstract class AbstractSpectrogramDemo extends AbstractDemo {
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);
  private static final Color COLOR_SHAPE = new Color(160, 160, 160, 192);
  private static final GridRender GRID_RENDER = new GridRender(Subdivide.of(0, 100, 10));
  // ---
  private final PathRender pathRenderCurve = new PathRender(COLOR_CURVE);
  private final PathRender pathRenderShape = new PathRender(COLOR_SHAPE);
  // ---
  private static final ScalarUnaryOperator MAGNITUDE_PER_SECONDS = QuantityMagnitude.SI().in("s^-1");
  // ---
  protected final GokartPoseSpec gokartPoseSpec;
  protected Tensor _control = null;

  protected AbstractSpectrogramDemo(GokartPoseSpec gokartPoseSpec, Object object) {
    super(gokartPoseSpec, object);
    this.gokartPoseSpec = gokartPoseSpec;
    gokartPoseSpec.symi = this instanceof BufferedImageSupplier;
    fieldsEditor(0).addUniversalListener(this::updateState);
    timerFrame.geometricComponent.addRenderInterfaceBackground(GRID_RENDER);
    // ---
    timerFrame.geometricComponent.setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = control();
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = gokartPoseSpec.manifoldDisplays.manifoldDisplay();
    final Tensor shape = manifoldDisplay.shape().multiply(markerScale());
    boolean conv = gokartPoseSpec.conv;
    if (gokartPoseSpec.data) {
      pathRenderCurve.setCurve(control, false).render(geometricLayer, graphics);
      Color fill = conv //
          ? new Color(255, 128, 128, 32)
          : new Color(255, 128, 128, 64);
      Color draw = conv //
          ? new Color(255, 128, 128, 128)
          : new Color(255, 128, 128, 255);
      new PointsRender(fill, draw) //
          .show(manifoldDisplay::matrixLift, shape, control) //
          .render(geometricLayer, graphics);
    }
    Tensor refined = protected_render(geometricLayer, graphics);
    // ---
    if (this instanceof BufferedImageSupplier bufferedImageSupplier && //
        gokartPoseSpec.symi)
      graphics.drawImage(bufferedImageSupplier.bufferedImage(), 0, 0, null);
    // ---
    graphics.setStroke(new BasicStroke(1f));
    if (conv) {
      pathRenderShape.setCurve(refined, false).render(geometricLayer, graphics);
      for (Tensor point : refined) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
        Path2D path2d = geometricLayer.toPath2D(shape);
        path2d.closePath();
        graphics.setColor(COLOR_SHAPE);
        graphics.fill(path2d);
        graphics.setColor(Color.BLACK);
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    RenderQuality.setDefault(graphics);
    if (gokartPoseSpec.diff)
      differences_render(graphics, manifoldDisplay, refined, gokartPoseSpec.spec);
  }

  public Scalar markerScale() {
    return RealScalar.of(0.2);
  }

  protected void updateState() {
    _control = gokartPoseSpec.getPoses();
  }

  // @Override
  protected final Tensor control() {
    return Tensor.of(_control.stream().map(gokartPoseSpec.manifoldDisplays.manifoldDisplay()::xya2point)).unmodifiable();
  }

  /** @return */
  protected abstract String plotLabel();

  private static final ColorDataGradient COLOR_DATA_GRADIENT = //
      ColorDataGradients.VISIBLE_SPECTRUM.deriveWithOpacity(RealScalar.of(0.75));
  private static final int MAGNIFY = 4;

  // @Override
  protected final void differences_render( //
      Graphics2D graphics, ManifoldDisplay manifoldDisplay, Tensor refined, boolean spectrogram) {
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    if (geodesicSpace instanceof LieGroup lieGroup) {
      LieDifferences lieDifferences = new LieDifferences(lieGroup);
      Scalar sampleRate = MAGNITUDE_PER_SECONDS.apply(gokartPoseSpec.gpd().getSampleRate());
      Tensor speeds = lieDifferences.apply(refined).multiply(sampleRate);
      if (0 < speeds.length()) {
        int dimensions = speeds.get(0).length();
        Show show = new Show();
        show.setPlotLabel(plotLabel());
        // show.getAxisX().setLabel("sample no.");
        Tensor domain = Range.of(0, speeds.length());
        final int width = timerFrame.geometricComponent.jComponent.getWidth();
        int offset_y = 0;
        for (int index = 0; index < dimensions; ++index) {
          Tensor signal = speeds.get(Tensor.ALL, index).unmodifiable();
          show.add(new ListPlot(domain, signal));
          // ---
          if (spectrogram) {
            ScalarUnaryOperator window = gokartPoseSpec.kernel.get();
            Tensor image = Spectrogram.vector(signal, window, COLOR_DATA_GRADIENT);
            BufferedImage bufferedImage = ImageFormat.of(image);
            int wid = bufferedImage.getWidth() * MAGNIFY;
            int hgt = bufferedImage.getHeight() * MAGNIFY;
            graphics.drawImage(bufferedImage, width - wid, offset_y, wid, hgt, null);
            offset_y += hgt + MAGNIFY;
          }
        }
        // Showable jFreeChart = ListPlot.of(show);
        int dwidth = 80 + speeds.length();
        int height = 400;
        show.render(graphics, new Rectangle( //
            dimension.width - dwidth, dimension.height - height, //
            80 + speeds.length(), height));
      }
    }
  }

  protected abstract Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics);
}
