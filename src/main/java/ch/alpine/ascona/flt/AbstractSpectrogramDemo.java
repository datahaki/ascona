// code by ob, jph
package ch.alpine.ascona.flt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascona.dat.gok.GokartPosParam;
import ch.alpine.ascona.dat.gok.GokartPoseDatas;
import ch.alpine.ascona.dat.gok.PosHz;
import ch.alpine.ascony.api.BufferedImageSupplier;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.fig.plt.Spectrogram;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.lie.LieDifferences;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.fft.SpectrogramArray;
import ch.alpine.tensor.fft.SpectrogramArrays;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.qty.UnitSystem;
import ch.alpine.tensor.sca.win.WindowFunctions;

abstract class AbstractSpectrogramDemo extends ManifoldDisplayDemo {
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);
  private static final Color COLOR_SHAPE = new Color(160, 160, 160, 192);

  @ReflectionMarker
  static class SpecParam {
    public Boolean diff = true;
    public Boolean spec = true;
    public Boolean data = true;
    public Boolean conv = true;
    public Boolean symi = false;
    public WindowFunctions kernel = WindowFunctions.GAUSSIAN;
    @FieldClip(min = "0", max = "10")
    public Integer radius = 2;
  }

  protected final GokartPosParam gokartPosParam;
  protected final SpecParam specParam;
  private PosHz posHz = null;

  protected AbstractSpectrogramDemo(Object object) {
    this(new SpecParam(), object);
  }

  private AbstractSpectrogramDemo(SpecParam specParam, Object object) {
    super(gokartPosParam = new GokartPosParam(), this.specParam = specParam, object);
    // gokartPoseSpec.symi = this instanceof BufferedImageSupplier;
    fieldsEditor(gokartPosParam).addUniversalListener(this::updateState);
    updateState();
    // ---
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
    geometricComponent().setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
  }

  @Override
  protected final List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_R2;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final Tensor control = posHz.getPoseSequence().getGeodesicControlPoints(manifoldDisplay()).unmodifiable();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    boolean conv = specParam.conv;
    if (specParam.data) {
      new PathRender(COLOR_CURVE, 1, control, false).render(geometricLayer, graphics);
      ColorPair colorPair = conv ? ColorPair.ASC : ColorPair.ASN;
      manifoldDisplay.showPoints(colorPair, markerScale(), control) //
          .render(geometricLayer, graphics);
    }
    final Tensor refined = process(control);
    // ---
    if (this instanceof BufferedImageSupplier bufferedImageSupplier && //
        specParam.symi) {
      Optional<BufferedImage> optional = Optional.ofNullable(bufferedImageSupplier.bufferedImage());
      if (optional.isPresent())
        graphics.drawImage(optional.orElseThrow(), 0, 0, null);
    }
    // ---
    graphics.setStroke(new BasicStroke(1f));
    if (conv) {
      new PathRender(COLOR_SHAPE, 1, refined, false).render(geometricLayer, graphics);
      manifoldDisplay.showPoints(ColorPair.ASD, markerScale(), refined) //
          .render(geometricLayer, graphics);
    }
    if (specParam.diff)
      differences_render(graphics, manifoldDisplay, refined, specParam.spec);
  }

  protected String plotLabel() {
    WindowFunctions windowFunctions = specParam.kernel;
    int radius = specParam.radius;
    return windowFunctions + " [" + (2 * radius + 1) + "]";
  }

  public final Scalar markerScale() {
    return RealScalar.of(0.2);
  }

  protected final void updateState() {
    posHz = gokartPosParam.getPosHz();
  }

  private static final ColorDataGradient COLOR_DATA_GRADIENT = //
      ColorDataGradients.VISIBLE_SPECTRUM.deriveWithOpacity(RealScalar.of(0.75));

  // @Override
  protected final void differences_render( //
      Graphics2D graphics, ManifoldDisplay manifoldDisplay, Tensor refined, boolean spectrogram) {
    final Dimension dimension = getSize();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    if (geodesicSpace instanceof LieGroup lieGroup) {
      TensorUnaryOperator lieDifferences = LieDifferences.of(lieGroup);
      Scalar sampleRate = UnitSystem.SI().apply(posHz.getSamplingRate());
      Tensor speeds = lieDifferences.apply(refined).multiply(sampleRate);
      if (0 < speeds.length()) {
        int dims = speeds.get(0).length();
        Show show = new Show();
        show.setPlotLabel(plotLabel());
        // show.getAxisX().setLabel("sample no.");
        Tensor domain = Range.of(0, speeds.length()).divide(sampleRate);
        String[] labels = { "vx", "vy", "va" };
        for (int index = 0; index < dims; ++index) {
          Tensor signal = speeds.get(Tensor.ALL, index).unmodifiable();
          show.add(ListLinePlot.of(domain, signal)).setLabel(labels[index]);
          // ---
          if (spectrogram) {
            ScalarUnaryOperator window = specParam.kernel.get();
            SpectrogramArray spectrogramArray = SpectrogramArrays.FOURIER.operator().config(window);
            Show show2 = new Show();
            show2.add(Spectrogram.of(spectrogramArray, signal, sampleRate, COLOR_DATA_GRADIENT));
            show2.render_autoIndent(graphics, new Rectangle( //
                2 * dimension.width / 3, index * dimension.height / 3, dimension.width / 3, dimension.height / 3));
          }
        }
        show.render_autoIndent(graphics, new Rectangle( //
            0, dimension.height / 2, 2 * dimension.width / 3, dimension.height / 2));
      }
    }
  }

  protected abstract Tensor process(Tensor control);
}
