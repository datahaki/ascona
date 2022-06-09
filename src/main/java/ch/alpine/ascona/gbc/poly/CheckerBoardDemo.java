// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

import javax.imageio.ImageIO;

import ch.alpine.ascona.lev.LogWeightingBase;
import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.PolygonCoordinates;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.dis.H2Display;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.ren.ArrayPlotRender;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.SpinnerListener;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.N;

/** transfer weights from barycentric coordinates defined by set of control points
 * in the square domain (subset of R^2) to means in non-linear spaces */
public class CheckerBoardDemo extends LogWeightingBase //
    implements SpinnerListener<ManifoldDisplay> {
  public static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._000.strict();

  // ---
  @ReflectionMarker
  public static class Param {
    public ParameterizationPattern pattern = ParameterizationPattern.CHECKER_BOARD;
    @FieldInteger
    @FieldClip(min = "50", max = "360")
    public Scalar refine = RealScalar.of(50);
    @FieldInteger
    @FieldClip(min = "2", max = "20")
    public Scalar factor = RealScalar.of(5);
    public Boolean freeze = false;

    public Scalar factor() {
      return N.DOUBLE.apply(factor);
    }
  }

  private final Param param = new Param();
  private Tensor reference;

  public CheckerBoardDemo() {
    super(true, ManifoldDisplays.ARRAYS, PolygonCoordinates.list());
    spinnerLogWeighting.addSpinnerListener(v -> recompute());
    setMidpointIndicated(true);
    FieldsEditor fieldsEditor = ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    fieldsEditor.addUniversalListener(this::recompute);
    // ---
    ManifoldDisplay manifoldDisplay = R2Display.INSTANCE;
    actionPerformed(manifoldDisplay);
    addSpinnerListener(this);
    addSpinnerListener(l -> recompute());
    recompute();
    // ---
    addMouseRecomputation();
  }

  public void actionPerformed(ActionEvent actionEvent) {
    System.out.println("export");
    if (param.freeze) {
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
              param.factor()));
          HsArrayPlot geodesicArrayPlot = manifoldDisplay().arrayPlot();
          // TODO ASCONA ALG redundant
          Tensor matrix = geodesicArrayPlot.raster(512, tensorUnaryOperator, DoubleScalar.INDETERMINATE);
          BufferedImage bufferedImage = ArrayPlotRender.rescale(matrix, COLOR_DATA_INDEXED, 1).export();
          ImageIO.write(bufferedImage, "png", new File(folder, logWeighting.toString() + ".png"));
          // RenderQuality.setDefault(graphics); // default so that raster becomes visible
          // Tensor pixel2model = geodesicArrayPlot.pixel2model(new Dimension(bufferedImage.getHeight(), bufferedImage.getHeight()));
          // ImageRender.of(bufferedImage, pixel2model).render(geometricLayer, graphics);
        } catch (Exception exception) {
          exception.printStackTrace();
        }
    }
  }

  private BufferedImage bufferedImage;

  @Override
  protected void recompute() {
    if (param.freeze) {
      System.out.println("compute");
      Tensor sequence = getGeodesicControlPoints();
      HsArrayPlot geodesicArrayPlot = manifoldDisplay().arrayPlot();
      Tensor matrix = geodesicArrayPlot.raster( //
          param.refine.number().intValue(), //
          function(sequence, reference.multiply(param.factor())), //
          DoubleScalar.INDETERMINATE);
      bufferedImage = ArrayPlotRender.rescale(matrix, COLOR_DATA_INDEXED, 1).bufferedImage();
    } else {
      bufferedImage = null;
    }
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.CORNERS, true));
    RenderQuality.setQuality(graphics);
    // ---
    if (param.freeze) {
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      if (Objects.isNull(bufferedImage))
        recompute();
      if (Objects.nonNull(bufferedImage)) {
        RenderQuality.setDefault(graphics); // default so that raster becomes visible
        new ImageRender(bufferedImage, manifoldDisplay.coordinateBoundingBox()).render(geometricLayer, graphics);
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

  @Override
  public void actionPerformed(ManifoldDisplay manifoldDisplay) {
    if (manifoldDisplay instanceof R2Display) {
      setControlPointsSe2(Tensors.fromString( //
          "{{0.287, -0.958, 0.000}, {-1.017, -0.953, 0.000}, {-0.717, 0.229, 0.000}, {-0.912, 0.669, 0.000}, {-0.644, 0.967, 0.000}, {0.933, 0.908, 0.000}, {0.950, -0.209, 0.000}, {-0.461, 0.637, 0.000}, {0.956, -0.627, 0.000}}"));
    } else
      if (manifoldDisplay instanceof H2Display) {
        setControlPointsSe2(Tensors.fromString( //
            "{{0.783, -2.467, 0.000}, {-0.083, -1.667, 0.000}, {-2.683, -1.167, 0.000}, {-2.650, 0.133, 0.000}, {-1.450, 2.467, 0.000}, {0.083, 0.033, 0.000}, {0.867, 2.383, 0.000}, {2.217, 2.500, 0.000}, {2.183, -0.517, 0.000}}"));
      } else //
        if (manifoldDisplay instanceof S2Display) {
          setControlPointsSe2(Tensors.fromString( //
              "{{-0.715, -0.357, 0.000}, {-0.708, 0.500, 0.000}, {-0.102, 0.592, 0.000}, {0.181, 0.892, 0.000}, {0.733, 0.455, 0.000}, {-0.349, 0.232, 0.000}, {-0.226, 0.008, 0.000}, {0.434, 0.097, 0.000}, {0.759, -0.492, 0.000}, {0.067, -0.712, 0.000}}"));
        }
  }

  @Override
  protected TensorUnaryOperator operator(Manifold manifold, Tensor sequence) {
    // biinvariant and variogram are not necessary
    return logWeighting().operator(null, manifold, null, sequence);
  }

  @Override
  protected TensorScalarFunction function(Tensor sequence, Tensor values) {
    TensorUnaryOperator operator = operator(sequence);
    TensorUnaryOperator dot_prod = point -> operator.apply(point).dot(values);
    return param.pattern.apply(dot_prod);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new CheckerBoardDemo().setVisible(1300, 900);
  }
}
