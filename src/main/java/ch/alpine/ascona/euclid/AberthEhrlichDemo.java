// code by jph
package ch.alpine.ascona.euclid;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Optional;

import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.BarLegend;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.fig.plt.ReliefPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.ComplexScalar;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.chq.FiniteScalarQ;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ImageResize;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.io.TableBuilder;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.num.ReIm;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.sca.Arg;
import ch.alpine.tensor.sca.ply.AberthEhrlich;
import ch.alpine.tensor.sca.ply.Polynomial;
import ch.alpine.tensor.sca.ply.Roots;

class AberthEhrlichDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    @FieldClip(min = "3", max = "20")
    @FieldSlider
    public Integer depth = 5;
    public Boolean radius = true;
    public Boolean relief = true;
    @FieldSelectionArray({ "20", "30", "50", "100", "150", "200", "300" })
    public transient Integer resolution = 100;
    public ColorDataGradients cdg = ColorDataGradients.HUE;
  }

  private final ShuffleFuse shuffleFuse;
  private final Param param;
  private Tensor complexZeros;

  public AberthEhrlichDemo() {
    super(param = new Param(), shuffleFuse = new ShuffleFuse());
    // ---
    fieldsEditor(shuffleFuse).addUniversalListener(this::shuffle);
    shuffle();
    setGeodesicControlPoints(RandomSample.of(manifoldDisplay().randomSampleInterface(), 3));
    geometricComponent().setRotatable(false);
    Tensor pvm = PvmBuilder.rhs().setOffset(500, 500).setPerPixel(RealScalar.of(100)).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  private static final TensorScalarFunction V2S = t -> ComplexScalar.of(t.Get(0), t.Get(1));
  private static final ScalarTensorFunction S2V = s -> ReIm.of(s).vector();

  private void shuffle() {
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    complexZeros = V2S.slash(RandomSample.of(randomSampleInterface, 100));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor seeds = V2S.slash(getGeodesicControlPoints());
    final int length = seeds.length();
    final Tensor _zeros = complexZeros.extract(0, length).unmodifiable();
    if (2 < length) {
      TensorScalarFunction tuo = tv -> {
        Scalar t = V2S.apply(tv);
        Tensor _seeds = seeds.copy();
        _seeds.set(t, 0);
        Tensor table = table(_zeros, _seeds, param.depth);
        return
        // table.get(Tensor.ALL, 0).stream() //
        // .map(Scalar.class::cast) //
        Flatten.scalars(table).map(Arg.FUNCTION) //
            .reduce(Scalar::add) //
            .filter(FiniteScalarQ::of) //
            .map(So2.MOD) //
            .orElse(DoubleScalar.INDETERMINATE);
      };
      ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tuo, DoubleScalar.INDETERMINATE);
      Show show = new Show();
      FontMetrics fontMetrics = graphics.getFontMetrics();
      Optional<Rectangle> optional = Show.optionalDefaultInsets(geometricComponent().getSize(), fontMetrics);
      if (optional.isPresent()) {
        Rectangle rectangle = optional.orElseThrow();
        CoordinateBoundingBox cbb = geometricLayer.fromRectangle(rectangle).orElseThrow();
        Tensor raster = manifoldDisplay.d2Raster().of(arrayFunction, cbb, param.resolution);
        raster = raster.maps(s -> FiniteScalarQ.of(s) ? s : RealScalar.ZERO);
        if (param.relief)
          show.add(ReliefPlot.of(raster, cbb, param.cdg));
        else {
          Rescale rescale = new Rescale(raster);
          BufferedImage bufferedImage = ImageFormat.of(rescale.result().maps(param.cdg));
          show.add(ImagePlot.of(bufferedImage, ImageResize.DEGREE_3, cbb, //
              new BarLegend(rescale.clip(), param.cdg), false, RealScalar.ONE));
        }
        show.render(graphics, geometricLayer.toRectangle(cbb).orElseThrow());
      }
    }
    {
      Tensor sequence = _zeros.maps(S2V);
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      manifoldDisplay.showPoints(ColorPair.REFERENCE, RealScalar.ONE, sequence) //
          .render(geometricLayer, graphics);
      leversRender.renderIndexP("z");
    }
    if (1 < length) {
      if (param.radius) {
        Scalar bound = bounds(_zeros, seeds);
        new PathRender(ColorStroke.SECONDARY_CURVE, CirclePoints.of(70).multiply(bound), true) //
            .render(geometricLayer, graphics);
      }
      Tensor table = table(_zeros, seeds, param.depth);
      int dimension1 = Unprotect.dimension1(table);
      // IO.println(Pretty.of(table.maps(Round._1)));
      for (int index = 0; index < dimension1; ++index) {
        TensorUnaryOperator tuo = manifoldDisplay::point2xya;
        Tensor points = tuo.slash(table.get(Tensor.ALL, index).maps(S2V));
        new PathRender(ColorStroke.TRACE, points, false).render(geometricLayer, graphics);
      }
    }
  }

  private static Scalar bounds(Tensor zeros, Tensor seeds) {
    int length = Integers.requireEquals(zeros.length(), seeds.length());
    Polynomial polynomial = zeros.stream() //
        .limit(length) //
        .map(Scalar.class::cast) //
        .map(zero -> Tensors.of(zero.negate(), zero.one())) //
        .map(Polynomial::of) //
        .reduce(Polynomial::times) //
        .orElseThrow();
    return Roots.bound(polynomial.coeffs());
  }

  private static Tensor table(Tensor zeros, Tensor seeds, int depth) {
    int length = Integers.requireEquals(zeros.length(), seeds.length());
    Polynomial polynomial = zeros.stream() //
        .limit(length) //
        .map(Scalar.class::cast) //
        .map(zero -> Tensors.of(zero.negate(), zero.one())) //
        .map(Polynomial::of) //
        .reduce(Polynomial::times) //
        .orElseThrow();
    TableBuilder tableBuilder = new TableBuilder();
    tableBuilder.appendRow(seeds);
    try {
      AberthEhrlich aberthEhrlich = new AberthEhrlich(polynomial, seeds);
      for (int i = 0; i < depth; ++i) {
        Tensor iterate = aberthEhrlich.iterate();
        tableBuilder.appendRow(iterate);
      }
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
    return tableBuilder.getTable();
  }

  static void main() {
    new AberthEhrlichDemo().runStandalone();
  }
}
