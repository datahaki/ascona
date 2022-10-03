// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.C1Display;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.io.TableBuilder;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.sca.Arg;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.ply.AberthEhrlich;
import ch.alpine.tensor.sca.ply.Polynomial;

public class AberthEhrlichDemo extends ControlPointsDemo {
  private static final PointsRender POINTS_RENDER_0 = //
      new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 255));

  // private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  // private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(182);
  // private static final Stroke STROKE = //
  // new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  // private static final CoordinateBoundingBox BOX = CoordinateBoundingBox.of( //
  // Clips.interval(-0.22, 0.53), //
  // Clips.interval(-0.22, 0.22));
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

  public void shuffle() {
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    zeros = RandomSample.of(randomSampleInterface, 10);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor seeds = getGeodesicControlPoints();
    final int length = seeds.length();
    {
      Clip clip = Clips.absolute(Pi.VALUE);
      TensorUnaryOperator tuo = t -> ColorDataGradients.HUE.apply(clip.rescale(Arg.FUNCTION.apply((Scalar) t)));
      ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tuo, Array.zeros(4));
      Tensor raster = D2Raster.of(C1Display.INSTANCE, 50, arrayFunction);
      BufferedImage bufferedImage = ImageFormat.of(raster);
      new ImageRender(bufferedImage, C1Display.INSTANCE.coordinateBoundingBox()).render(geometricLayer, graphics);
    }
    // if (2 <= length) {
    // }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    // for (Tensor p : seeds) {
    // geometricLayer.pushMatrix(manifoldDisplay.matrixLift(p));
    // Tensor point2xy = manifoldDisplay.point2xy(p);
    // Point2D point2d = geometricLayer.toPoint2D(point2xy);
    // int pix = (int) point2d.getX();
    // int piy = (int) point2d.getY();
    // graphics.drawRect(pix - 2, piy - 2, 5, 5);
    // geometricLayer.popMatrix();
    // }
    LeversRender leversRender = LeversRender.of(C1Display.INSTANCE, zeros.extract(0, length), null, geometricLayer, graphics);
    leversRender.renderSequence(POINTS_RENDER_0);
    leversRender.renderIndexP("z");
    if (1 < length) {
      Polynomial polynomial = zeros.stream() //
          .limit(length) //
          .map(Scalar.class::cast) //
          .map(s -> Tensors.of(s.negate(), s.one())) //
          .map(Polynomial::of) //
          .reduce(Polynomial::times) //
          .orElseThrow();
      // System.out.println(polynomial);
      TableBuilder tableBuilder = new TableBuilder();
      tableBuilder.appendRow(seeds);
      try {
        AberthEhrlich aberthEhrlich = new AberthEhrlich(polynomial, seeds);
        for (int i = 0; i < param.depth; ++i) {
          Tensor iterate = aberthEhrlich.iterate();
          tableBuilder.appendRow(iterate);
        }
      } catch (Exception exception) {
        System.out.println(exception.getMessage());
      }
      Tensor table = tableBuilder.getTable();
      int dimension1 = Unprotect.dimension1(table);
      for (int index = 0; index < dimension1; ++index) {
        PathRender pathRender = new PathRender(Color.BLACK);
        Tensor points = table.get(Tensor.ALL, index).map(manifoldDisplay::point2xya);
        pathRender.setCurve(points, false).render(geometricLayer, graphics);
      }
    }
    // Tensor weights = ConstantArray.of(RationalScalar.of(1, length), length);
    // ManifoldDisplay manifoldDisplay = manifoldDisplay();
    // HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    // BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._03);
    // Tensor mean = null;
    // try {
    // mean = biinvariantMean.mean(sequence, weights);
    // } catch (Exception e) {
    // System.err.println("mean does not exist");
    // }
    // graphics.setColor(Color.LIGHT_GRAY);
    //// graphics.setStroke(STROKE);
    // RenderQuality.setQuality(graphics);
    //// if (Objects.nonNull(mean))
    //// for (Tensor point : sequence) {
    //// Tensor curve = Subdivide.of(0, 1, 20).map(homogeneousSpace.curve(point, mean));
    //// graphics.draw(geometricLayer.toPath2D(Tensor.of(curve.stream().map(manifoldDisplay::point2xy))));
    //// }
    // graphics.setStroke(new BasicStroke(1));
  }

  public static void main(String[] args) {
    launch();
  }
}
