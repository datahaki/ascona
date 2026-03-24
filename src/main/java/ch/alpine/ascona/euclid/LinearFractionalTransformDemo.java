// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Meshgrid;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.api.Genesis;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophis.var.VariogramFunctions;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.img.ImageResize;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.jet.LinearFractionalTransform;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

class LinearFractionalTransformDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    public ThreePointScalings tps = ThreePointScalings.MEAN_VALUE;
    public ImageResize imgRes = ImageResize.DEGREE_0;
    public Integer resw = 40;
    public Integer resh = 30;
    @FieldFuse
    public Boolean fuse = false;
  }

  private final BufferedImage bufferedImage = ResourceData.bufferedImage("ch/alpine/ascona/image/album_it.jpg");
  private final Param param;
  private final Tensor src;
  private final Tensor reference;
  private final Interpolation interpolation;

  public LinearFractionalTransformDemo() {
    super(param = new Param());
    setTitle("" + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
    src = ImageFormat.from(bufferedImage);
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();
    reference = Tensors.matrixInt( //
        new int[][] { { 0, 0 }, { width, 0 }, { width, height }, { 0, height } }) //
        .maps(RealScalar.of(-0.5)::add);
    interpolation = LinearInterpolation.of(src);
    setGeodesicControlPoints(Tensors.fromString("{{46.25, 28.0}, {132.0, 1.0}, {132.0, 99.0}, {46.75, 51.0}}"));
    geometricComponent().setModel2Pixel(PvmBuilder.rhs().setOffset(100, 500).setPerPixel(4).digest());
    geometricComponent().addRenderInterfaceBackground(new GridRender(geometricComponent()::getSize));
    geometricComponent().addRenderInterfaceBackground(AxesRender.INSTANCE);
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  public static TensorUnaryOperator toImagePixel(int h) {
    return p -> Tensors.of(RealScalar.of(h).subtract(p.Get(1)), p.Get(0));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param.fuse) {
      param.fuse = false;
      IO.println(getGeodesicControlPoints());
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of( //
        Clips.positive(bufferedImage.getWidth()), //
        Clips.positive(bufferedImage.getHeight()));
    ImageRender imageRender = new ImageRender(bufferedImage, cbb);
    imageRender.render(geometricLayer, graphics);
    Tensor sequence = getGeodesicControlPoints();
    Tensor points = toImagePixel(bufferedImage.getHeight()).slash(sequence);
    LinearFractionalTransform lft = LinearFractionalTransform.fit(reference, points);
    new PathRender(ColorStroke.CONVEX_HULL, sequence, true).render(geometricLayer, graphics);
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
    leversRender.renderIndexP();
    leversRender.renderMatrix2(Tensors.vector(0, 0, 0), lft.matrix());
    Dimension dimension = geometricComponent().getSize();
    dimension.width /= 2;
    dimension.height /= 3;
    Meshgrid meshgrid = new Meshgrid( //
        CoordinateBoundingBox.of( //
            Clips.positive(bufferedImage.getWidth()), //
            Clips.positive(bufferedImage.getHeight())), //
        param.resw, param.resh);
    {
      Show show = new Show();
      show.setShowLabel("Linear Fractional Transform");
      TensorUnaryOperator tuo = lft.andThen(interpolation::get);
      show.add(ImagePlot.of(ImageFormat.of(imageOrTransparent(meshgrid, tuo)), param.imgRes));
      show.render_autoIndent(graphics, new Rectangle(dimension.width, 0, dimension.width, dimension.height));
    }
    {
      Show show = new Show();
      show.setShowLabel("Three Point: " + param.tps);
      Genesis genesis = ThreePointCoordinate.of(param.tps);
      TensorUnaryOperator tuo = p -> {
        Tensor ref = Tensor.of(reference.stream().map(v -> v.subtract(p)));
        return interpolation.get(genesis.origin(ref).dot(points));
      };
      show.add(ImagePlot.of(ImageFormat.of(imageOrTransparent(meshgrid, tuo)), param.imgRes));
      show.render_autoIndent(graphics, new Rectangle(dimension.width, dimension.height, dimension.width, dimension.height));
    }
    {
      Show show = new Show();
      show.setShowLabel("Inv. Dist Coordinate");
      HomogeneousSpace homogeneousSpace = manifoldDisplay().homogeneousSpace();
      Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(homogeneousSpace);
      LogWeightings logWeightings = LogWeightings.COORDINATE;
      ScalarUnaryOperator vf = VariogramFunctions.INVERSE_POWER.of(RealScalar.TWO);
      // ---
      Tensor movingOrigin = reference;
      Sedarim sedarim = logWeightings.sedarim(biinvariant, vf, movingOrigin);
      Tensor weights = meshgrid.image(sedarim::sunder);
      BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean();
      AveragedMovingDomain2D averagedMovingDomain2D = new AveragedMovingDomain2D(weights, biinvariantMean, ZEROS);
      TensorUnaryOperator tuo = interpolation::get;
      Tensor lift = averagedMovingDomain2D.forward(points, safeWrap(tuo));
      show.add(ImagePlot.of(ImageFormat.of(lift), param.imgRes));
      show.render_autoIndent(graphics, new Rectangle(dimension.width, dimension.height * 2, dimension.width, dimension.height));
    }
  }

  private static final Tensor ZEROS = Array.zeros(4);

  private static TensorUnaryOperator safeWrap(TensorUnaryOperator tuo) {
    return x -> {
      try {
        return tuo.apply(x);
      } catch (Exception exception) {
        return ZEROS;
      }
    };
  }

  private static Tensor imageOrTransparent(Meshgrid meshgrid, TensorUnaryOperator tuo) {
    return meshgrid.image(safeWrap(tuo));
  }

  static void main() {
    new LinearFractionalTransformDemo().runStandalone();
  }
}
