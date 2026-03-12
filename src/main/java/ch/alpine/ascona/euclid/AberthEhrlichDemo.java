// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.DensityPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.ComplexScalar;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataGradients;
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
    @FieldFuse
    public Boolean shuffle = false;
    public Integer resolution = 30;
    public ColorDataGradients cdg = ColorDataGradients.HUE;
  }

  private final Param param;
  private Tensor complexZeros;

  public AberthEhrlichDemo() {
    super(param = new Param());
    // ---
    fieldsEditor(0).addUniversalListener(() -> {
      if (param.shuffle) {
        param.shuffle = false;
        shuffle();
      }
    });
    shuffle();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    setGeodesicControlPoints(RandomSample.of(randomSampleInterface, 3));
    geometricComponent().setRotatable(false);
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
    Tensor seeds = V2S.slash(getGeodesicControlPoints());
    final int length = seeds.length();
    if (2 < length) {
      Tensor _zeros = complexZeros.extract(0, length);
      TensorUnaryOperator tuo = tv -> {
        Scalar t = V2S.apply(tv);
        Tensor _seeds = seeds.copy();
        _seeds.set(t, 0);
        Tensor table = table(_zeros, _seeds, param.depth);
        try {
          // return table.get(Tensor.ALL, 0).stream() //
          // .map(Scalar.class::cast) //
          // .map(Abs.FUNCTION) //
          // .reduce(Scalar::add) //
          // .orElseThrow();
          return table.get(Tensor.ALL, 0).stream() //
              .map(Scalar.class::cast) //
              .map(Arg.FUNCTION) //
              .reduce(Scalar::add) //
              .orElseThrow();
          // return table.flatten(1) //
          // .map(Scalar.class::cast) //
          // .map(Abs.FUNCTION) //
          // .reduce(Scalar::add) //
          // .orElseThrow();
        } catch (Exception e) {
          // e
        }
        return DoubleScalar.INDETERMINATE;
      };
      ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tuo, DoubleScalar.INDETERMINATE);
      CoordinateBoundingBox cbb = manifoldDisplay().d2Raster_coordinateBoundingBox();
      Tensor raster = manifoldDisplay().d2Raster().of(arrayFunction, cbb, param.resolution);
      Show show = new Show();
      show.add(DensityPlot.of(raster, cbb, param.cdg));
      show.render(graphics, geometricLayer.toRectangle(cbb).orElseThrow());
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      Tensor sequence = complexZeros.extract(0, length).maps(S2V);
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      manifoldDisplay.showPoints(new Color(128, 128, 128, 64), new Color(128, 128, 128, 255), RealScalar.ONE, sequence) //
          .render(geometricLayer, graphics);
      leversRender.renderIndexP("z");
    }
    if (1 < length) {
      {
        Scalar bound = bounds(complexZeros.extract(0, length), seeds);
        PathRender pathRender = new PathRender(Color.RED);
        pathRender.setCurve(CirclePoints.of(70).multiply(bound), true) //
            .render(geometricLayer, graphics);
      }
      Tensor table = table(complexZeros.extract(0, length), seeds, param.depth);
      int dimension1 = Unprotect.dimension1(table);
      // IO.println(Pretty.of(table.maps(Round._1)));
      for (int index = 0; index < dimension1; ++index) {
        PathRender pathRender = new PathRender(Color.BLACK);
        TensorUnaryOperator tuo = manifoldDisplay::point2xya;
        Tensor points = tuo.slash(table.get(Tensor.ALL, index).maps(S2V));
        pathRender.setCurve(points, false).render(geometricLayer, graphics);
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
