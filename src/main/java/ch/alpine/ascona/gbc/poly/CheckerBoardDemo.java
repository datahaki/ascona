// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

import ch.alpine.ascony.api.Box2D;
import ch.alpine.ascony.api.LogWeighting;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.api.PolygonCoordinates;
import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.arp.D2Raster;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.var.VariogramFunctions;

/** transfe
 * weights from barycentric coordinates defined by set of control points*in the square domain(subset of R^2)to means in non-linear spaces */
public class CheckerBoardDemo extends ControlPointsDemo { // FIXME ASCONA SPIN
  public static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._000.strict();

  // ---
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.d2Rasters());
      manifoldDisplays = ManifoldDisplays.S2;
    }
  }

  @ReflectionMarker
  public static class Param1 {
    public ParameterizationPattern pattern = ParameterizationPattern.CHECKER_BOARD;
    @FieldClip(min = "50", max = "360")
    public Integer refine = 50;
    @FieldClip(min = "2", max = "20")
    public Integer factor = 5;
    public Boolean freeze = false;
  }

  private final Param0 param0;
  private final Param1 param1;
  private Tensor reference;

  public CheckerBoardDemo() {
    this(new Param0(), new Param1());
  }

  public CheckerBoardDemo(Param0 param, Param1 param1) {
    super(param, param1);
    this.param0 = param;
    this.param1 = param1;
    controlPointsRender.setMidpointIndicated(true);
    // ---
    // fieldsEditor(0).addUniversalListener(this::spun);
    // fieldsEditor(1).addUniversalListener(this::recompute);
    // spun();
  }
  // public CheckerBoardDemo() {
  // super(true, ManifoldDisplays.metricD2Rasters(), PolygonCoordinates.list());
  // spinnerLogWeighting.addSpinnerListener(v -> recompute());
  // FieldsEditor fieldsEditor = ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
  // fieldsEditor.addUniversalListener(this::recompute);
  // ---
  // spun(ManifoldDisplays.R2);
  // addManifoldListener(this);
  // addManifoldListener(l -> recompute());
  // recompute();
  // ---
  // }

  public void actionPerformed(ActionEvent actionEvent) {
    System.out.println("export");
    if (param1.freeze) {
      Tensor sequence = getGeodesicControlPoints();
      // LeversRender leversRender = LeversRender.of( //
      // geodesicDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
      // leversRender.renderSurfaceP();
      File folder = HomeDirectory.Pictures(CheckerBoardDemo.class.getSimpleName());
      folder.mkdir();
      for (LogWeighting logWeighting : PolygonCoordinates.list())
        try {
          System.out.println(logWeighting);
          TensorScalarFunction tensorUnaryOperator = function(sequence, reference.multiply( //
              DoubleScalar.of(param1.factor)));
          ManifoldDisplay manifoldDisplay = manifoldDisplay();
          D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
          // TODO ASCONA ALG redundant
          ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, DoubleScalar.INDETERMINATE);
          Tensor matrix = D2Raster.of(hsArrayPlot, 512, arrayFunction);
          // BufferedImage bufferedImage = ArrayPlotRender.rescale(matrix, COLOR_DATA_INDEXED, 1, false).bufferedImage();
          // ImageIO.write(bufferedImage, "png", new File(folder, logWeighting.toString() + ".png"));
          // RenderQuality.setDefault(graphics); // default so that raster becomes visible
          // Tensor pixel2model = geodesicArrayPlot.pixel2model(new Dimension(bufferedImage.getHeight(), bufferedImage.getHeight()));
          // ImageRender.of(bufferedImage, pixel2model).render(geometricLayer, graphics);
        } catch (Exception exception) {
          exception.printStackTrace();
        }
    }
  }

  private BufferedImage bufferedImage;

  // @Override
  protected void recompute() {
    if (param1.freeze) {
      System.out.println("compute");
      Tensor sequence = getGeodesicControlPoints();
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
      TensorScalarFunction tsf = function(sequence, reference.multiply(DoubleScalar.of(param1.factor)));
      ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
      Tensor matrix = D2Raster.of(hsArrayPlot, param1.refine, arrayFunction);
      Showable showable = ArrayPlot.of(matrix, COLOR_DATA_INDEXED);
      Show show = new Show();
      show.add(showable);
      bufferedImage = show.image(new Dimension(200, 200));
    } else {
      bufferedImage = null;
    }
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.CORNERS, true));
    RenderQuality.setQuality(graphics);
    // ---
    if (param1.freeze) {
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      if (Objects.isNull(bufferedImage))
        recompute();
      if (Objects.nonNull(bufferedImage)) {
        RenderQuality.setDefault(graphics); // default so that raster becomes visible
        new ImageRender(bufferedImage, hsArrayPlot.coordinateBoundingBox()) //
            .render(geometricLayer, graphics);
      }
    } else {
      reference = getGeodesicControlPoints();
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, reference, null, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      bufferedImage = null;
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  int resolution() {
    return 120; // for sequence of length 6
  }
  // @Override
  // public void spun(ManifoldDisplays manifoldDisplays) {
  // if (manifoldDisplays.equals(ManifoldDisplays.R2)) {
  // setControlPoints(Tensors.fromString( //
  // "{{0.287, -0.958, 0.000}, {-1.017, -0.953, 0.000}, {-0.717, 0.229, 0.000}, {-0.912, 0.669, 0.000}, {-0.644, 0.967, 0.000}, {0.933, 0.908, 0.000}, {0.950,
  // -0.209, 0.000}, {-0.461, 0.637, 0.000}, {0.956, -0.627, 0.000}}"));
  // } else if (manifoldDisplays.equals(ManifoldDisplays.H2)) {
  // setControlPointsSe2(Tensors.fromString( //
  // "{{0.783, -2.467, 0.000}, {-0.083, -1.667, 0.000}, {-2.683, -1.167, 0.000}, {-2.650, 0.133, 0.000}, {-1.450, 2.467, 0.000}, {0.083, 0.033, 0.000},
  // {0.867,2.383, 0.000}, {2.217, 2.500, 0.000}, {2.183, -0.517, 0.000}}"));
  // } else //
  // if (manifoldDisplays.equals(ManifoldDisplays.S2)) {
  // setControlPointsSe2(Tensors.fromString( //
  // "{{-0.715, -0.357, 0.000}, {-0.708, 0.500, 0.000}, {-0.102, 0.592, 0.000}, {0.181, 0.892, 0.000}, {0.733, 0.455, 0.000}, {-0.349, 0.232, 0.000},
  // {-0.226,0.008, 0.000}, {0.434, 0.097, 0.000}, {0.759, -0.492, 0.000}, {0.067, -0.712, 0.000}}"));
  // }
  // }

  // @Override
  protected Sedarim operator(Tensor sequence) {
    // of biinvariant only hsDesign is used
    Manifold manifold = (Manifold) manifoldDisplay().geodesicSpace();
    Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(manifold);
    return LogWeightings.COORDINATE.sedarim(biinvariant, VariogramFunctions.GAUSSIAN.of(RealScalar.ONE), sequence);
  }

  //
  // @Override
  protected TensorScalarFunction function(Tensor sequence, Tensor values) {
    Sedarim sedarim = operator(sequence);
    TensorUnaryOperator dot_prod = point -> sedarim.sunder(point).dot(values);
    return param1.pattern.apply(dot_prod);
  }

  static void main() {
    launch();
  }
}
