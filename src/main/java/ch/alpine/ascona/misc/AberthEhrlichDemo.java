// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.arp.D2Raster;
import ch.alpine.ascony.dis.C1Display;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.io.TableBuilder;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.sca.Arg;
import ch.alpine.tensor.sca.ply.AberthEhrlich;
import ch.alpine.tensor.sca.ply.Polynomial;
import ch.alpine.tensor.sca.ply.Roots;

// TODO ASCONA REV what are these artifacts?
public class AberthEhrlichDemo extends ControlPointsDemo {
  private static final PointsRender POINTS_RENDER_0 = //
      new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 255));

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, List.of(ManifoldDisplays.C1));
    }

    @FieldClip(min = "3", max = "20")
    @FieldSlider
    public Integer depth = 5;
    @FieldFuse
    public Boolean shuffle = false;
    public Integer resolution = 30;
  }

  private final Param param;
  private Tensor zeros;

  public AberthEhrlichDemo() {
    this(new Param());
  }

  public AberthEhrlichDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    fieldsEditor(0).addUniversalListener(() -> {
      if (param.shuffle) {
        param.shuffle = false;
        shuffle();
      }
    });
    shuffle();
  }

  private void shuffle() {
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    zeros = RandomSample.of(randomSampleInterface, 10);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor seeds = getGeodesicControlPoints();
    final int length = seeds.length();
    if (2 < length) {
      Tensor _zeros = zeros.extract(0, length);
      TensorUnaryOperator tuo = t -> {
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
      Tensor raster = D2Raster.of(C1Display.INSTANCE, param.resolution, arrayFunction);
      Tensor rescal = new Rescale(raster).result().map(ColorDataGradients.HUE);
      BufferedImage bufferedImage = ImageFormat.of(rescal);
      new ImageRender(bufferedImage, C1Display.INSTANCE.coordinateBoundingBox()).render(geometricLayer, graphics);
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LeversRender leversRender = LeversRender.of(C1Display.INSTANCE, zeros.extract(0, length), null, geometricLayer, graphics);
    leversRender.renderSequence(POINTS_RENDER_0);
    leversRender.renderIndexP("z");
    if (1 < length) {
      {
        Scalar bound = bounds(zeros.extract(0, length), seeds);
        PathRender pathRender = new PathRender(Color.RED);
        pathRender.setCurve(CirclePoints.of(70).multiply(bound), true) //
            .render(geometricLayer, graphics);
      }
      Tensor table = table(zeros.extract(0, length), seeds, param.depth);
      int dimension1 = Unprotect.dimension1(table);
      for (int index = 0; index < dimension1; ++index) {
        PathRender pathRender = new PathRender(Color.BLACK);
        Tensor points = table.get(Tensor.ALL, index).map(manifoldDisplay::point2xya);
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
    launch();
  }
}
